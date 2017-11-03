package com.meetup.auth.util

/**
 * Directly from Chapstick codebase
 *
 * Provides functionality similar to Scala's `lazy val`, but avoids a
 * thread synchronization issue. (In short, using a `lazy val` inside
 * a function that may be called concurrently for the same object
 * instance is a bad idea, because all callers share one lock.)
 */
private[auth] class Lazy[A](x: => A) {
  private lazy val underlyingLazy: A = x
  def get(): A = underlyingLazy
}

private[auth] object Lazy {
  def apply[A](x: => A): Lazy[A] = new Lazy(x)
}
