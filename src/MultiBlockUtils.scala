package lb.multiUtil

import lb.util._

import Math._

object multiUtils {

	def getNextFlatDomain(nextLattice:Lattice2D,dir:Int,orient:Int) : Box2D = {
		val vicinity = nextLattice.D.vicinity
		val nx = nextLattice.nX-1
		val ny = nextLattice.nY-1
		val nextDomain = {
			if (dir == 0) {
				if (orient == 1) new Box2D(0,vicinity-1,vicinity,ny-vicinity)
				else new Box2D(nx-(vicinity-1),nx,vicinity,ny-vicinity)
			}
			else {
				if (orient == 1) new Box2D(vicinity,nx-vicinity,0,vicinity-1)
				else new Box2D(vicinity,nx-vicinity,ny-(vicinity-1),ny)
			}
		}
		
		nextDomain
	}

	def getNextCornerDomain(nextLattice:Lattice2D,xNormal:Int,yNormal:Int) : Box2D = {
		val vicinity = nextLattice.D.vicinity
		val nx = nextLattice.nX-1
		val ny = nextLattice.nY-1
		val nextDomain = {
			if (xNormal == 1) {
				if (yNormal == 1) new Box2D(0,vicinity-1,0,vicinity-1)
				else new Box2D(0,vicinity-1,ny-(vicinity-1),ny)
			}
			else {
				if (yNormal == 1) new Box2D(nx-(vicinity-1),nx,0,vicinity-1)
				else new Box2D(nx-(vicinity-1),nx,ny-(vicinity-1),ny)
			}
		}

		nextDomain
	}

	def copyCellsDomain(lattice:Lattice2D,domain:Box2D,cellsDomain:Array[Array[Cell]]) : Unit = {
		for (iX <- domain.x0 to domain.x1; iY <- domain.y0 to domain.y1) {
			val relX = iX - domain.x0
			val relY = iY-domain.y0
			lattice(iX,iY).dyn = cellsDomain(relX)(relY).dyn.copy
			for (iPop <- 0 until lattice.D.q) lattice(iX,iY)(iPop) = cellsDomain(relX)(relY)(iPop)
		}
	}
    
    
}