package com.phishscan

import com.typesafe.scalalogging.LazyLogging
import okhttp3.{MediaType, OkHttpClient, Request, RequestBody, Response}

object Main extends LazyLogging{
  def main(args: Array[String]): Unit = {

    val client = new OkHttpClient.Builder().build()

    val mediaType = MediaType.parse("application/json")
    val body = RequestBody.create(mediaType, """
                                               |{
                                               | "contents": [
                                               |  {
                                               |   "parts": [
                                               |    {
                                               |     "text": "Is the following sentence positive sentiment or negative\n\nPasta was cold"
                                               |    }
                                               |   ]
                                               |  }
                                               | ],
                                               | "generationConfig": {
                                               |  "temperature": 0.9,
                                               |  "topK": 1,
                                               |  "topP": 1,
                                               |  "maxOutputTokens": 2048,
                                               |  "stopSequences": []
                                               | },
                                               | "safetySettings": [
                                               |  {
                                               |   "category": "HARM_CATEGORY_HARASSMENT",
                                               |   "threshold": "BLOCK_MEDIUM_AND_ABOVE"
                                               |  },
                                               |  {
                                               |   "category": "HARM_CATEGORY_HATE_SPEECH",
                                               |   "threshold": "BLOCK_MEDIUM_AND_ABOVE"
                                               |  },
                                               |  {
                                               |   "category": "HARM_CATEGORY_SEXUALLY_EXPLICIT",
                                               |   "threshold": "BLOCK_MEDIUM_AND_ABOVE"
                                               |  },
                                               |  {
                                               |   "category": "HARM_CATEGORY_DANGEROUS_CONTENT",
                                               |   "threshold": "BLOCK_MEDIUM_AND_ABOVE"
                                               |  }
                                               | ]
                                               |}""".stripMargin)

    val request = new Request.Builder()
      .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-1.0-pro:generateContent?key=AIzaSyAv2BDoSXQClkmArzJe-uNBxCJXohxBmds")
      .method("POST", body)
      .addHeader("Content-Type", "application/json")
      .build()

    val response: Response = client.newCall(request).execute()

    // Handle the response as needed
    println(response.body().string())

    // Close the response to release resources
    response.close()
  }
}
