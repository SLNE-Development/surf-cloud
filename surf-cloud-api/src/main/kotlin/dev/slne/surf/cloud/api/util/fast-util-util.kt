package dev.slne.surf.cloud.api.util

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectSet
import it.unimi.dsi.fastutil.objects.ObjectSets
import org.jetbrains.annotations.Unmodifiable
import org.jetbrains.annotations.UnmodifiableView

fun <T> ObjectSet<T>.synchronize(): ObjectSet<T> = ObjectSets.synchronize(this)
fun <T> ObjectSet<T>.freeze(): @UnmodifiableView ObjectSet<T> = ObjectSets.unmodifiable(this)
fun <T> mutableObjectSetOf(vararg elements: T) = ObjectOpenHashSet(elements)
fun <T> mutableObjectSetOf() = ObjectOpenHashSet<T>()
fun <T> objectSetOf(vararg elements: T) = mutableObjectSetOf(elements).freeze()
fun <T> objectSetOf() = emptyObjectSet<T>()
fun <T> emptyObjectSet(): @Unmodifiable ObjectSet<T> = ObjectSets.emptySet()
fun <T> Sequence<T>.toMutableObjectSet() = ObjectOpenHashSet(toList())
fun <T> Sequence<T>.toObjectSet() = toMutableObjectSet().freeze()