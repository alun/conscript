package conscript

import com.ning.http.client.Response

import net.liftweb.json._
import dispatch.as

object LiftJson {
  def As(res: Response) = JsonParser.parse(as.String(res))
}
