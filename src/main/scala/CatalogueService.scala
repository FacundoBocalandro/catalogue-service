import catalogueservice.catalogueService.CatalogueServiceGrpc.{CatalogueService, CatalogueServiceStub}
import catalogueservice.catalogueService.{Product, ProductListRequest, ProductListResponse}
import io.grpc.{ManagedChannelBuilder, ServerBuilder}

import scala.io._
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

class MyService extends CatalogueService {
  val productDatabase = new CSVReader()

  override def productList(request: ProductListRequest): Future[ProductListResponse] = {
    val reply = ProductListResponse(products = productDatabase.getProductsByCountry(request.country))
    Future.successful(reply)
  }
}

trait LocationDatabase {
  def getProductsByCountry(country: String): List[Product]
}

class CSVReader extends LocationDatabase {

  import CSVReader._

  val data: List[Product] = getProducts("catalogue.csv")

  override def getProductsByCountry(country: String): List[Product] = {
    data.filter(prod => prod.country.equals(country))
  }
}

object CSVReader {

  def getProducts(filePath: String): List[Product] = {
    val fileSource = Source.fromInputStream(getClass.getClassLoader.getResourceAsStream(filePath))
    val data: List[List[String]] = fileSource.getLines().toList.map(_.split(',').toList)
    println(data)
    fileSource.close()
    data.map {
      case List(id, name, price, country) =>
        Product(id, name, price, country)
    }
  }

}

object CatalogueServiceServer extends App {
  val builder = ServerBuilder.forPort(sys.env.getOrElse("port", "50001").toInt)

  builder.addService(
    CatalogueService.bindService(new MyService(), ExecutionContext.global)
  )

  val server = builder.build()
  server.start()

  println(s"Running on port ${sys.env.getOrElse("port", "not specified")}....")
  server.awaitTermination()
}