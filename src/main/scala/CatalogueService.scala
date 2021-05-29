import auth_service.AuthServiceGrpc.{AuthService, AuthServiceStub}
import auth_service.{AuthServiceGrpc, GetAuthenticationResponse, Status, User}
import io.grpc.{ManagedChannelBuilder, ServerBuilder}

import scala.io._
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

class MyService extends CatalogueService {
  val locationDatabase = new CSVReader()

  override def catalogue(request: User): Future[ProductListResponse] = {
    val reply = ProductListResponse(products = locationDatabase.authenticate(request))
    Future.successful(reply)
  }
}

trait LocationDatabase {
  def authenticate(user: User): Status

}

class CSVReader extends LocationDatabase {

  import CSVReader._

  val data: List[Product] = getProducts("catalogue.csv")

  def authenticate(user: User): Status = {
    data.find(_ == user) match {
      case Some(_) => Status.SUCCESS
      case None => Status.FAIL
    }
  }
}

object CSVReader {

  def getProducts(filePath: String): List[Product] = {
    val fileSource = Source.fromInputStream(getClass.getClassLoader.getResourceAsStream(filePath))
    val data: List[List[String]] = fileSource.getLines().toList.map(_.split(',').toList)
    fileSource.close()
    data.map {
      case List(id, name) =>
        Product(id = id, name = name)
    }
  }

}

object CatalogueServiceServer extends App {
  val builder = ServerBuilder.forPort(sys.env.getOrElse("port", "5000").toInt)

  builder.addService(
    CatalogueService.bindService(new MyService(), ExecutionContext.global)
  )

  val server = builder.build()
  server.start()

  println(s"Running on port ${sys.env.getOrElse("port", "not specified")}....")
  server.awaitTermination()
}