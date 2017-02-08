package com.ws3d.chan

import com.ws3d.chan.HttpExceptions.{BoardNotFoundException, ServerException}
import com.ws3d.chan.model.{ChanPage, ChanPost, ChanThreadDetailed, ChanThreadHeader}
import org.scalatest.FunSuite

import scalaj.http.HttpResponse

class ChanTest extends FunSuite {

  object ChanImpl extends Chan

  test("getImagePosts should return a List of ChanPost objects that have tim and ext assigned") {
    val chanPostWithImage = ChanPost(1, Some(123L), Some(".jpg"))
    val chanPostWithoutImage = ChanPost(1, None, None)
    val input = ChanThreadDetailed(List(
      chanPostWithImage,
      chanPostWithoutImage
    ))

    val actual = ChanImpl.getImagePosts(input)
    val expected = List(chanPostWithImage)

    assert(actual == expected)
  }

  test("getPages should return List of ChanPages") {
    val customResponse = HttpResponse[String](
      """
        |[
        | {
        |   "page": 3,
        |   "threads": [
        |     {
        |       "no": 1,
        |       "last_modified": 2
        |     }
        |   ]
        | }
        |]
      """.stripMargin, 200, Map())
    val uriToResponse = (uri: String) => customResponse

    val actual = ChanImpl.getPages("s", uriToResponse)
    val expected = List(ChanPage(3, Some(List(ChanThreadHeader(1, 2)))))

    assert(actual == expected)
  }

  test("getPages should throw BoardNotFoundException when response code is 404") {
    val customResponse = HttpResponse[String]("", 404, Map())
    val uriToResponse = (uri: String) => customResponse

    assertThrows[BoardNotFoundException] {
      ChanImpl.getPages("x", uriToResponse)
    }
  }

  test("getPages should throw ServerException when response code is >= 500") {
    val customResponse = HttpResponse[String]("", 503, Map())
    val uriToResponse = (uri: String) => customResponse

    assertThrows[ServerException] {
      ChanImpl.getPages("z", uriToResponse)
    }
  }

  test("getThreadDetailed should return ChanThreadDetailed") {
    val customResponse = HttpResponse[String](
      """
        |{
        | "posts": [
        |   {
        |     "no": 1,
        |     "tim": 123456,
        |     "ext": ".jpg"
        |   }
        | ]
        |}
      """.stripMargin, 200, Map())
    val uriToResponse = (uri: String) => customResponse

    val actual = ChanImpl.getThreadDetailed(ChanThreadHeader(1, 2), "s", uriToResponse)
    val expected = ChanThreadDetailed(List(ChanPost(1, Some(123456), Some(".jpg"))))

    assert(actual == expected)
  }

  test("getRandomPage should return one of the pages from given list") {
    val page1 = ChanPage(1, None)
    val page2 = ChanPage(2, None)
    val page3 = ChanPage(3, None)
    val chanBoard = List(page1, page2, page3)

    val randomPage = ChanImpl.getRandomPage(chanBoard)

    assert(chanBoard contains randomPage)
  }

  test("getRandomThreadHeader should return one of the page's thread headers") {
    val header1 = ChanThreadHeader(1, 1)
    val header2 = ChanThreadHeader(2, 2)
    val header3 = ChanThreadHeader(3, 3)
    val headers = List(header1, header2, header3)
    val chanPage = ChanPage(1, Some(headers))

    val randomThreadHeader = ChanImpl.getRandomThreadHeader(chanPage)

    assert(headers contains randomThreadHeader)
  }

  test("getRanomThreadHeader should return ChanThreadHeader(0, 0) if page had no threads") {
    val chanPage = ChanPage(1, None)

    val randomThreadHeader = ChanImpl.getRandomThreadHeader(chanPage)

    assert(randomThreadHeader == ChanThreadHeader(0, 0))
  }
}
