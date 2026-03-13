package xyz.alikindsys.vendor.microstatement;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryAttributeHolder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.IdMapper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import org.spongepowered.asm.logging.ILogger;
import org.spongepowered.asm.service.MixinService;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class StateRefresher {
    public static final ExecutorService THREAD_POOL = Executors.newCachedThreadPool();
    public static StateRefresher INSTANCE = new StateRefresher();
    private static final ILogger LOGGER = MixinService.getService().getLogger("microstatement");

    // Oh lord this is just a bunch of interface delegation.
    // I like generics but this seems excessive. I'll later look if this can be monomorphised.
    public <O, S extends StateHolder<O, S>, V extends Comparable<V>> Collection<S> addProperty(final Supplier<StateDefinition<O, S>> definitionProvider, final IdMapper<S> idMapper, final Property<V> property, final V defaultValue) {
        @SuppressWarnings("unchecked") final RefreshableStateManager<O, S> manager = ((RefreshableStateManager<O, S>) definitionProvider.get());

        manager.statement_addProperty(property, defaultValue);

        @SuppressWarnings("unchecked") final StatementPropertyExtensions<V> p = (StatementPropertyExtensions<V>) property;
        final List<V> nonDefaultValues = p.statement_getValues().stream().filter(v -> v != defaultValue).toList();

        final Collection<S> states = manager.statement_reconstructStateList(Collections.singletonMap(property, nonDefaultValues));

        for (final S s : states) {
            idMapper.add(s);
            ((StatementStateExtensions<?>) s).statement_initShapeCache();
        }

        return states;
    }

    public <O, S extends StateHolder<O, S>, V extends Comparable<V>> void refreshBlockStates(final Property<V> property, final Collection<V> addedValues, final Collection<V> removedValues) {
        refreshStates(
                BuiltInRegistries.BLOCK, Block.BLOCK_STATE_REGISTRY,
                property, addedValues, removedValues,
                Block::defaultBlockState, Block::getStateDefinition, s -> ((StatementBlockStateExtensions) s).statement_initShapeCache()
        );
        RegistryAttributeHolder.get(BuiltInRegistries.BLOCK).addAttribute(RegistryAttribute.MODDED);
    }

    public <O, S extends StateHolder<O, S>, V extends Comparable<V>> void refreshStates(final Iterable<O> registry, final IdMapper<S> idMapper, Property<V> property, final Collection<V> addedValues, final Collection<V> removedValues, final Function<O, S> defaultStateGetter, final Function<O, StateDefinition<O, S>> stateDefinitionGetter, final Consumer<S> newStateConsumer) {
        final long startTime = System.nanoTime();
        final List<RefreshableStateManager<O, S>> managersToRefresh = new LinkedList<>();

        for (final O entry : registry) {
            if (((StatementStateExtensions<?>) defaultStateGetter.apply(entry)).statement_getEntries().containsKey(property)) {
                @SuppressWarnings("unchecked") final RefreshableStateManager<O, S> manager = (RefreshableStateManager<O, S>) stateDefinitionGetter.apply(entry);

                managersToRefresh.add(manager);
            }
        }

        final Map<Property<V>, Collection<V>> addedValueMap = new HashMap<>();

        final Collection<S> addedStates = new ConcurrentLinkedQueue<>();
        final Collection<S> removedStates = new ConcurrentLinkedQueue<>();

        final Collection<CompletableFuture<?>> allFutures = new LinkedList<>();

        final int entryQuantity = managersToRefresh.size();

        final int addedValueQuantity = addedValues.size();
        final int removedValueQuantity = removedValues.size();

        final boolean noAddedValues = addedValueQuantity == 0;
        final boolean noRemovedValues = removedValueQuantity == 0;


        if (noAddedValues && noRemovedValues) {
            LOGGER.debug("Refreshing states of {} entries after {} ns of setup.", entryQuantity, System.nanoTime() - startTime);
        } else if (noAddedValues || noRemovedValues) {
            LOGGER.debug("Refreshing states of {} entries for {} values(s) {} after {} ns of setup.", entryQuantity, noRemovedValues ? "new" : "removed", noRemovedValues ? addedValues : removedValues, System.nanoTime() - startTime);
        } else {
            LOGGER.debug("Refreshing states of {} entries for new values(s) {} and removed value(s) {} after {} ns of setup.", entryQuantity, addedValues, removedValues, System.nanoTime() - startTime);
        }

        synchronized (property) {
            MutableProperty.of(property).ifPresent(mutableProperty -> {
                addedValueMap.put(property, addedValues);

                @SuppressWarnings("unchecked") final StatementPropertyExtensions<V> p = (StatementPropertyExtensions<V>) mutableProperty;
                final Collection<V> values = p.statement_getValues();
                values.addAll(addedValues);
                values.removeAll(removedValues);
            });
        }

        synchronized (idMapper) {
            for (final RefreshableStateManager<O, S> manager : managersToRefresh) {
                allFutures.add(CompletableFuture.supplyAsync(() -> {
                    if (!noRemovedValues) {
                        @SuppressWarnings("unchecked") final StateDefinition<O, S> f = ((StateDefinition<O, S>) manager);
                        f.getPossibleStates().parallelStream().filter(state -> state.getValues().containsKey(property) && removedValues.contains(state.getValue(property))).forEach(removedStates::add);
                    }

                    return manager.statement_reconstructStateList(addedValueMap);
                }, THREAD_POOL).thenAccept(addedStates::addAll));
            }

            CompletableFuture.allOf(allFutures.toArray(CompletableFuture<?>[]::new)).thenAccept(v -> {
                addedStates.forEach(state -> {
                    newStateConsumer.accept(state);
                    idMapper.add(state);
                });

                final int addedStateQuantity = addedStates.size();
                final int removedStateQuantity = removedStates.size();

                final boolean noAdditions = addedStateQuantity == 0;
                final boolean noRemovals = removedStateQuantity == 0;

                if (noAdditions && noRemovals) {
                    LOGGER.debug("Refreshed states with no additions or removals after {} ms.", System.nanoTime() - startTime);
                } else if (noAdditions || noRemovals) {
                    LOGGER.debug("{} {} state(s) for {} values(s) {} after {} ms.", noRemovals ? "Added" : "Removed", noRemovals ? addedStateQuantity : removedStateQuantity, noRemovals ? "new" : "old", noRemovals ? addedValues : removedValues, (System.nanoTime() - startTime) / 1_000_000);
                } else {
                    LOGGER.debug("Added {} state(s) for new values(s) {} and removed {} states for old value(s) {} after {} ms.", addedStateQuantity, addedValues, removedStateQuantity, removedValues, (System.nanoTime() - startTime) / 1_000_000);
                }
            }).join();
        }
    }

    public <O, S extends StateHolder<O, S>, V extends Comparable<V>> void reorderStates(final Iterable<O> registry, final IdMapper<S> idMapper, final Function<O, StateDefinition<O, S>> stateDefinitionGetter) {
        // TODO: Maybe setting autoread is important. Who am I to know, if this doesn't work then I'll add it.
        final Iterable<O> entries;
        if (registry instanceof Registry<O> registry1)
        {
            final Int2ObjectMap<O> sortedEntries = new Int2ObjectRBTreeMap<>();

            for (final O entry : registry)
            {
                sortedEntries.put(registry1.getId(entry), entry);
            }

            entries = sortedEntries.values();
        } else {
            entries = registry;
        }

        final Collection<S> initialStates = new LinkedList<>();

        for (final O entry : entries)
        {
            initialStates.addAll(stateDefinitionGetter.apply(entry).getPossibleStates());
        }

        initialStates.forEach(idMapper::add);
    }

    public boolean isParallel()
    {
        // It will be parallel unless ferritecore is loaded, meaning it'll probably crash with other mods.
        return !FabricLoader.getInstance().isModLoaded("ferritecore");
    }

    public StateRefresher() {

    }
}
