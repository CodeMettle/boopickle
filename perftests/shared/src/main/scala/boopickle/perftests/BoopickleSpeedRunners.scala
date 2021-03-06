package boopickle.perftests

import java.nio.ByteBuffer

import boopickle.Default._
import boopickle.{BufferPool, DecoderSpeed, EncoderSpeed}

abstract class BooSpeedRunner[A](data: A) extends TestRunner[A](data) {
  override def name = "BooPickle!"
}

object BooPickleSpeedRunners {

  implicit def pickleState   = new PickleState(new EncoderSpeed, false, false)
  implicit def unpickleState = (b: ByteBuffer) => new UnpickleState(new DecoderSpeed(b), false, false)

  def encodeRunner[A](data: A)(implicit p: Pickler[A]): BooSpeedRunner[A] = new BooSpeedRunner[A](data) {
    var testData: A = _

    override def initialize = {
      BufferPool.enable()
      testData = data
      val bb = Pickle.intoBytes(testData)
      val ba = new Array[Byte](bb.limit)
      bb.get(ba)
      ba
    }

    override def run(): Unit = {
      val bb = Pickle.intoBytes(testData)
      BufferPool.release(bb)
      ()
    }
  }

  def decodeRunner[A](data: A)(implicit p: Pickler[A]): BooSpeedRunner[A] = new BooSpeedRunner[A](data) {
    var testData: A    = _
    var bb: ByteBuffer = _

    override def initialize = {
      BufferPool.enable()
      testData = data
      bb = Pickle.intoBytes(testData)
      val ba = new Array[Byte](bb.limit)
      bb.get(ba)
      bb.rewind()
      ba
    }

    override def run: Unit = {
      Unpickle[A].fromBytes(bb)
      bb.rewind()
      ()
    }
  }
}
