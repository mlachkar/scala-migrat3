package incompat

object ScalafixIncompat12 {

  trait Pretty {
    val print: String
  }

  object Pretty {
    def pretty[A](a: A)(implicit ev: A => Pretty): String = ev.apply(a).print
  }

  object Test extends App {
    assert(Pretty.pretty[String]("foo")(str => new Pretty {
      val print: String = str
    }) == "foo")
  }

}