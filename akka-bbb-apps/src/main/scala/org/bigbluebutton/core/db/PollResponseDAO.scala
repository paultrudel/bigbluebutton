package org.bigbluebutton.core.db

import org.bigbluebutton.core.models.Poll
import slick.jdbc.PostgresProfile.api._

case class PollResponseDbModel(
    pollId:    String,
    optionId:  Option[Int],
    meetingId: Option[String],
    userId:    Option[String]
)

class PollResponseDbTableDef(tag: Tag) extends Table[PollResponseDbModel](tag, None, "poll_response") {
  val pollId = column[String]("pollId")
  val optionId = column[Option[Int]]("optionId")
  val meetingId = column[Option[String]]("meetingId")
  val userId = column[Option[String]]("userId")
  val * = (pollId, optionId, meetingId, userId).<>(PollResponseDbModel.tupled, PollResponseDbModel.unapply)
}

object PollResponseDAO {
  def insert(poll: Poll, meetingId: String, userId: String, seqOptionIds: Seq[Int]) = {

    //Clear previous responses of the user and add all
    DatabaseConnection.enqueue(
      TableQuery[PollResponseDbTableDef]
        .filter(_.pollId === poll.id)
        .filter(_.meetingId === meetingId)
        .filter(_.userId === userId)
        .delete
    )

    for {
      optionId <- seqOptionIds
    } yield {
      DatabaseConnection.enqueue(
        TableQuery[PollResponseDbTableDef].forceInsert(
          PollResponseDbModel(
            pollId = poll.id,
            optionId = Some(optionId),
            meetingId = {
              if (poll.isSecret) None else Some(meetingId)
            },
            userId = {
              if (poll.isSecret) None else Some(userId)
            }
          )
        )
      )
    }

    //When Secret, insert the user in a different row just to inform the he answered already
    if (poll.isSecret) {
      DatabaseConnection.enqueue(
        TableQuery[PollResponseDbTableDef].forceInsert(
          PollResponseDbModel(
            pollId = poll.id,
            optionId = None,
            meetingId = Some(meetingId),
            userId = Some(userId)
          )
        )
      )
    }
  }
}