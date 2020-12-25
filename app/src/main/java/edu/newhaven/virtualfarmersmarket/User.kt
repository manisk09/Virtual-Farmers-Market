package edu.newhaven.virtualfarmersmarket

import java.io.Serializable
import java.io.StringBufferInputStream

class MyUser {
  var emailAddress: String = ""
  var phoneNbr: String = ""
  var preferredName: String = ""
  var searchLimit: String = ""
  var userID: String = ""
  var latitude: Double = 0.0
  var longitude: Double = 0.0

  constructor(
    emailAddress: String,
    phoneNbr: String,
    preferredName: String,
    searchLimit: String,
    userID: String
  ) {
    this.emailAddress = emailAddress
    this.phoneNbr = phoneNbr
    this.preferredName = preferredName
    this.searchLimit = searchLimit
    this.userID = userID
  }

}



