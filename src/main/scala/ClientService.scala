import catalogueservice.catalogueService.CatalogueServiceGrpc.CatalogueServiceStub
import catalogueservice.catalogueService.{CatalogueServiceGrpc, ProductListRequest, ProductListResponse}
import io.grpc.ManagedChannelBuilder

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}

object ClientService extends App {

  implicit val ec: ExecutionContextExecutor = ExecutionContext.global


  def createStub(ip: String, port: Int = 50000): CatalogueServiceStub = {
    val builder = ManagedChannelBuilder.forAddress(ip, port)
    builder.usePlaintext()
    val channel = builder.build()

    CatalogueServiceGrpc.stub(channel)
  }

  val stub1 = createStub("127.0.0.1", 5000)

  val response: Future[ProductListResponse] = stub1.productList(ProductListRequest(country = "Argentina")) //valid

  response.onComplete { r =>
    println("Response: " + r)
  }

  System.in.read()
}