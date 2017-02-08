package com.ws3d.chan.model

/**
  * There will always be at least one post per thread, so no need to use Option.
  * @param posts
  */
case class ChanThreadDetailed (posts: List[ChanPost])
