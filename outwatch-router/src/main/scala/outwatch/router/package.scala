package outwatch

import outwatch._
import colibri.ProSubject

package object router {
  type RouterStore[P] = ProSubject[Action, (Action, RouterState[P])]

  type RouterResolve[P] = PartialFunction[P, VDomModifier]
}
