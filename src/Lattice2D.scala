package lb

import lb.dyn._
import lb.select._
import lb.util.Arrays._
import lb.dataProcessors2D._

class Lattice2D( val D: Descriptor, val nX: Int, val nY: Int,
		 val defaultDynamics: Dynamics )  {
  
  private lazy val half = D.q/2
  private lazy val xRange = (0 until nX).toList
  private lazy val yRange = (0 until nY).toList
  private lazy val fRange = (1 to half).toList  
  
  var dataProcessors = List[DataProcessor2D]()

  private val grid = {
    val g = new Array[Array[Cell]](nX,nY)
    for( iX <- xRange; iY <- yRange ) {
      g(iX)(iY) = new Cell(defaultDynamics)
    }
    g
  }
  
  def apply( x: Int, y: Int ) = grid(x)(y)

  def collide() = { 
		var iX = 0
		while (iX < nX) {
			var iY = 0
			while (iY < nY) {
				grid(iX)(iY).collide
				// The collided populations are swapped with their opposite direction
				// this step is needed by the streaming step.
				grid(iX)(iY).revert
				iY += 1
			}
			iX += 1
    }
//     for (iX <- xRange; iY <- yRange) {
//       grid(iX)(iY).collide
//       // The collided populations are swapped with their opposite direction
//       // this step is needed by the streaming step.
//       grid(iX)(iY).revert
//     }
  }


 
  def stream() = {
		bulkStream(1,nX-2,1,nY-2)
		
		boundaryStream(0,    0,    0,    nY-1)
		boundaryStream(nX-1, nX-1, 0,    nY-1)
		boundaryStream(0,    nX-1, 0,    0)
		boundaryStream(0,    nX-1, nY-1, nY-1)
		
		periodicStream(-1, -1,    0,  nY-1)
    periodicStream( 0, nX-1, -1, -1)
    periodicStream(-1, -1,   -1, -1)
    periodicStream(-1, -1,    nY, nY)
		
		
// 		var iX = 0
// 		while (iX < nX) {
// 			var iY = 0
// 			while (iY < nY) {
// 				var iPop = 1
// 				while (iPop <= half) {
// 					// The modulo are used for default periodicity
// 					val nextX = (iX + D.c(iPop)(0) + nX) % nX
// 					val nextY = (iY + D.c(iPop)(1) + nY) % nY
// 					
// 					// swapping grid(iX,iY,iPop+half) with grid(nextX)(nextY)(iPop)
// 					val tmp = grid(iX)(iY)(iPop+half)
// 					grid(iX)(iY)(iPop+half) = grid(nextX)(nextY)(iPop)
// 					grid(nextX)(nextY)(iPop) = tmp
// 					iPop += 1
// 				}
// 				iY += 1
// 			}
// 			iX += 1
//     }

//     for (iX <- xRange; iY <- yRange; iPop <- fRange) {
//       // The modulo are used for default periodicity
//       val nextX = (iX + D.c(iPop)(0) + nX) % nX
//       val nextY = (iY + D.c(iPop)(1) + nY) % nY
//       
//       // swapping grid(iX,iY,iPop+half) with grid(nextX)(nextY)(iPop)
//       val tmp = grid(iX)(iY)(iPop+half)
//       grid(iX)(iY)(iPop+half) = grid(nextX)(nextY)(iPop)
//       grid(nextX)(nextY)(iPop) = tmp
//     }
  }
  
  def bulkStream(x0:Int, x1:Int, y0:Int, y1:Int) = {
		var iX = x0
		while (iX <= x1) {
			var iY = y0
			while (iY <= y1) {
				var iPop = 1
				while (iPop <= half) {
					// The modulo are used for default periodicity
					val nextX = iX + D.c(iPop)(0)
					val nextY = iY + D.c(iPop)(1)
					
					// swapping grid(iX,iY,iPop+half) with grid(nextX)(nextY)(iPop)
					val tmp = grid(iX)(iY)(iPop+half)
					grid(iX)(iY)(iPop+half) = grid(nextX)(nextY)(iPop)
					grid(nextX)(nextY)(iPop) = tmp
					iPop += 1
				}
				iY += 1
			}
			iX += 1
    }
	}
	
	def boundaryStream(x0:Int, x1:Int, y0:Int, y1:Int) = {
		var iX = x0
		while (iX <= x1) {
			var iY = y0
			while (iY <= y1) {
				var iPop = 1
				while (iPop <= half) {
					val nextX = iX + D.c(iPop)(0)
					val nextY = iY + D.c(iPop)(1)
					
					if (nextX >= x0 && nextX <= x1 && nextY >= y0 && nextY <= y1) {
						val tmp = grid(iX)(iY)(iPop+half)
						grid(iX)(iY)(iPop+half) = grid(nextX)(nextY)(iPop)
						grid(nextX)(nextY)(iPop) = tmp
					}
					iPop += 1
				}
				iY += 1
			}
			iX += 1
    }
	}
	
	def periodicStream(x0:Int, x1:Int, y0:Int, y1:Int) = {
		var iX = x0
		while (iX <= x1) {
			var iY = y0
			while (iY <= y1) {
				var iPop = 1
				while (iPop < D.q) {
					val prevX = iX - D.c(iPop)(0)
					val prevY = iY - D.c(iPop)(1)
					if ( (prevX >= 0 && prevX < nX) && (prevY >= 0 && prevY < nY) ) {
							val nextX = (iX+nX) % nX;
							val nextY = (iY+nY) % nY;
							
							val tmp = grid(prevX)(prevY)(D.opposite(iPop))
							grid(prevX)(prevY)(D.opposite(iPop)) = grid(nextX)(nextY)(iPop)
							grid(nextX)(nextY)(iPop) = tmp
					}
					iPop += 1
				}
				iY += 1
			}
			iX += 1
    }
    
		
	}

  def collideAndStream() = { collide; stream; dataProcess }
  
  def dataProcess() = { dataProcessors.foreach( _.apply() ) }

  def map[A]( f: Cell => A ) = {
    val ary = new Array[Array[A]](nX,nY)
    for( iX <- xRange; iY <- yRange) {
      ary(iX)(iY) = f( grid(iX)(iY) )
    }
    ary
  }
  
  def map[A]( f: (Int,Int,Cell) => A ) = {
    val ary = new Array[Array[A]](nX,nY)
    for( iX <- xRange; iY <- yRange) {
      ary(iX)(iY) = f(iX,iY,grid(iX)(iY))
    }
    ary
  }

  def select( region: Region ) = new ComplexSelection( this, region )
  def select( region: Rectangle ) = new RectangularSelection( this, region )
  def selectAll = select(WholeDomain)

  override lazy val toString = "Lattice("+D+", sizeX="+nX+", sizeY="+nY+")"
}
