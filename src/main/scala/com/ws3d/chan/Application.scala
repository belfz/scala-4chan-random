package com.ws3d.chan

import java.net.SocketTimeoutException

import com.ws3d.chan.HttpExceptions.{BoardNotFoundException, ServerException}

import scala.util.Random
import com.ws3d.chan.model.{ChanPage, ChanPost, ChanThreadDetailed, ChanThreadHeader}

import scalaj.http.{BaseHttp, Http, HttpResponse}
import org.json4s.DefaultFormats
import org.json4s.native.Serialization.read

trait Chan {
  val baseURI = "http://a.4cdn.org/"
  def boardURI (boardSymbol: String) = s"${baseURI}${boardSymbol}/threads.json";
  def threadURI (boardSymbol: String, no: String) = s"${baseURI}${boardSymbol}/thread/${no}.json"

  implicit val formats = DefaultFormats

  def getPages (boardSymbol: String, uriToResponse: String => HttpResponse[String]): List[ChanPage] = {
    println(s"Fetching from board /$boardSymbol/...")
    val boardResponse: HttpResponse[String] = uriToResponse(boardURI(boardSymbol))
    boardResponse.code match {
      case 200 => read[List[ChanPage]](boardResponse.body)
      case 404 => throw new BoardNotFoundException(s"board /$boardSymbol/ not found!")
      case x if x >= 500 => throw new ServerException("there was an internal server error!")
    }
  }

  def getThreadDetailed (randomThreadHeader: ChanThreadHeader, boardSymbol: String, uriToResponse: String => HttpResponse[String]): ChanThreadDetailed = {
    println(s"thread ${randomThreadHeader.no}...")
    val threadResponse: HttpResponse[String] = uriToResponse(threadURI(boardSymbol, randomThreadHeader.no.toString))
    read[ChanThreadDetailed](threadResponse.body)
  }

  def getImagePosts (threadDetailed: ChanThreadDetailed): List[ChanPost] = {
    threadDetailed.posts.filter({
      case ChanPost(_, Some(tim), Some(ext)) => true
      case _ => false
    })
  }

  def getRandomPage (chanBoardPages: List[ChanPage]): ChanPage = {
    val randomPageNumber = Random.nextInt(chanBoardPages.length)
    println(s"page $randomPageNumber...")
    chanBoardPages(randomPageNumber)
  }

  def getRandomThreadHeader (randomPage: ChanPage): ChanThreadHeader = {
    randomPage.threads.map(threads => {
      threads(Random.nextInt(threads.length))
    }).getOrElse(ChanThreadHeader(0, 0))
  }
}


object Application extends Chan {
  def main(args: Array[String]): Unit = {
    val boardSymbol = "s"
    try {
      val chanPages = getPages(boardSymbol, Http(_).timeout(3000, 3000).asString)
      val randomPage = getRandomPage(chanPages)
      val randomThread = getRandomThreadHeader(randomPage)
      val threadDetailed = getThreadDetailed(randomThread, boardSymbol, Http(_).timeout(3000, 3000).asString)
      val imagePosts = getImagePosts(threadDetailed)

      val posts = imagePosts match {
        case x::xs => Some(imagePosts)
        case List() => None
      }

      posts.map(imagePosts => {
        val imagePost = imagePosts(Random.nextInt(imagePosts.length))
        println(s"http://is2.4chan.org/${boardSymbol}/${imagePost.tim.get}${imagePost.ext.get}")
      })
    } catch {
      case e: SocketTimeoutException => println("Connection timed out.")
      case e: BoardNotFoundException => println(e.message)
      case e: ServerException => println(e.message)
    }
  }
}

/**
  * scalaj: https://github.com/scalaj/scalaj-http
  *
  * 4. add ability to save image to disk
  */
