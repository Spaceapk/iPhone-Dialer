package com.pablormlopez.iphonedialer
import com.pablormlopez.iphonedialer.domain.Search
import org.junit.Assert.*
import org.junit.Test
class SearchTest {
    @Test fun foldAccents() { assertEquals("jose nunez",Search.normalized("José Núñez")) }
    @Test fun digitsStripFormatting() { assertEquals("50588881234",Search.digits("+505 (8888)-1234")) }
    @Test fun t9Letters() { assertEquals("72256",Search.t9("Pablo")) }
    @Test fun t9Accents() { assertEquals("5673",Search.t9("José")) }
    @Test fun t9Spaces() { assertEquals("26262742",Search.t9("Ana María")) }
    @Test fun matchName() { assertTrue(Search.matches("José", emptyList(),"jose")) }
    @Test fun matchNumber() { assertTrue(Search.matches("Persona",listOf("+505 8888-1234"),"888812")) }
    @Test fun matchT9() { assertTrue(Search.matches("Pablo", emptyList(),"72256")) }
    @Test fun blankQuery() { assertTrue(Search.matches("", emptyList(),"")) }
    @Test fun noMatch() { assertFalse(Search.matches("Pablo",listOf("5550100"),"99999")) }
    @Test fun punctuationNotWildcard() { assertFalse(Search.matches("Pablo",emptyList(),"--")) }
    @Test fun validInternational() { assertTrue(Search.validDialString("+505 8888-1234")) }
    @Test fun validServiceCode() { assertTrue(Search.validDialString("*123#")) }
    @Test fun rejectEmpty() { assertFalse(Search.validDialString("  ")) }
    @Test fun rejectUriInjection() { assertFalse(Search.validDialString("5550100?phoneAccount=evil")) }
    @Test fun rejectScheme() { assertFalse(Search.validDialString("tel:5550100")) }
    @Test fun rejectOversize() { assertFalse(Search.validDialString("1".repeat(129))) }
    @Test fun zeroDuration() { assertEquals("00:00",Search.duration(0)) }
    @Test fun positiveDuration() { assertEquals("01:05",Search.duration(65)) }
    @Test fun negativeDurationClamped() { assertEquals("00:00",Search.duration(-100)) }
}
