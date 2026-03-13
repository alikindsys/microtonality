package xyz.alikindsys.vendor.microstatement;

import net.minecraft.world.level.block.state.properties.Property;

import java.util.Optional;

public interface MutableProperty<E extends Comparable<E>>
{
    @SuppressWarnings("unchecked")
    public static <E extends Comparable<E>> Optional<MutableProperty<E>> of(Property<E> property)
    {
        return property instanceof MutableProperty<?> ? Optional.of((MutableProperty<E>) property) : Optional.empty();
    }

    @SuppressWarnings("unchecked")
    default Property<E> asProperty()
    {
        return ((Property<E>) this);
    }
}