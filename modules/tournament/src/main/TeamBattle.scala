package lila.tournament

import play.api.data._
import play.api.data.Forms._

import lila.hub.lightTeam._
import lila.user.User

case class TeamBattle(
    teams: Set[TeamId],
    nbTopPlayers: Int
) {
  lazy val sortedTeamIds = teams.toList.sorted
}

object TeamBattle {

  def init(teamId: TeamId) = TeamBattle(Set(teamId), 5)

  case class RankedTeam(
      rank: Int,
      teamId: TeamId,
      topPlayers: List[TopPlayer]
  ) {
    def magicScore = topPlayers.foldLeft(0)(_ + _.magicScore)
    def score = topPlayers.foldLeft(0)(_ + _.score)
  }

  case class TopPlayer(userId: User.ID, magicScore: Int) {
    def score: Int = magicScore / 10000
  }

  object DataForm {
    import play.api.data.Forms._
    import lila.common.Form._

    val fields = mapping(
      "teams" -> nonEmptyText,
      "nbTopPlayers" -> number(min = 1, max = 10)
    )(Setup.apply)(Setup.unapply)
      .verifying("We need at least 2 teams", s => s.potentialTeamIds.size > 1)
      .verifying("In this version of team battles, no more than 10 teams can be allowed.", s => s.potentialTeamIds.size <= 10)

    def edit(teams: List[String], nbTopPlayers: Int) = Form(fields) fill
      Setup(s"${teams mkString "\n"}\n", nbTopPlayers)

    def empty = Form(fields)

    case class Setup(
        teams: String,
        nbTopPlayers: Int
    ) {
      def potentialTeamIds: Set[String] =
        teams.lines.map(_.takeWhile(' ' !=)).filter(_.nonEmpty).toSet
    }
  }
}