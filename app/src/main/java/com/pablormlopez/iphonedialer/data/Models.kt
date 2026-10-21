package com.pablormlopez.iphonedialer.data
data class Contact(val id: Long, val lookup: String, val name: String, val phones: List<String>, val starred: Boolean, val photo: String?)
data class Recent(val id: Long, val number: String, val name: String?, val type: Int, val date: Long, val duration: Long, val presentation: Int)
data class Blocked(val id: Long, val number: String)
