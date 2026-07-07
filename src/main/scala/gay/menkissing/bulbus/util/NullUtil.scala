package gay.menkissing.bulbus.util

object NullUtil:
  // Flexible type jank, yes I want it to be nullable
  extension[T](flexible: T | Null)
    def nullable: T | Null = flexible
    
