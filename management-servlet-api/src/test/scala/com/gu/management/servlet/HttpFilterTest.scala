package com.gu.management.servlet

import com.gu.management._
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse, HttpServletRequestWrapper }
import javax.servlet.{ ServletResponse, ServletRequest, FilterChain }
import org.scalatest
import scalatest.FlatSpec
import scalatest.matchers.ShouldMatchers
import scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers._
import scala.collection.JavaConverters._
import scala.collection.JavaConversions._
import java.util
import net.liftweb.mocks.MockHttpServletRequest
import com.gu.management.HttpRequest
import servlet.ServletHttpResponse

class FakeManagementPage(override val path: String, val response: Response, override val needsAuth: Boolean) extends ManagementPage {
  override def dispatch = { case _ => response }
  override def canDispatch(request: HttpRequest) = true
  override def get(req: HttpRequest): Response = response
}

class HttpFilterTest extends FlatSpec with ShouldMatchers with MockitoSugar {
  "ManagementFilter" should "pass onto page if path matches" in {
    val mockRequest = new MockHttpServletRequest("/foo")
    val mockResponse = mock[Response]
    val mockHttpResponse = mock[HttpServletResponse]
    val finalChain = mock[FilterChain]

    // Given a page that does not require authentication
    val testPage = new FakeManagementPage("/foo", mockResponse, false)
    val testFilter = new ManagementFilter {
      val userProvider = null
      val applicationName = "foo"
      val pages = testPage :: Nil
    }

    // When we call doHttpFilter
    testFilter.doHttpFilter(mockRequest, mockHttpResponse, finalChain)
    // It should call the page to render to the response
    verify(mockResponse).sendTo(any[ServletHttpResponse]())
    // It should not call the chain further
    verifyNoMoreInteractions(finalChain)
    verifyNoMoreInteractions(mockResponse)
  }

  it should "return a 401 Unauthorised for unauthorised access of the path" in {
    val mockRequest = new MockHttpServletRequest("/foo")
    val mockResponse = mock[Response]
    val mockHttpResponse = mock[HttpServletResponse]
    val finalChain = mock[FilterChain]
    // Given a page that requires authorization
    val testPage = new FakeManagementPage("/foo", mockResponse, true)
    val testFilter = new ManagementFilter {
      val userProvider = null
      val applicationName = "foo"
      val pages = testPage :: Nil
    }

    // When we call doHttpFilter
    testFilter.doHttpFilter(mockRequest, mockHttpResponse, finalChain)
    // It should call the page to render to the response
    verify(mockHttpResponse).sendError(401, "Needs Authorisation")
    // It should not call the chain further
    verifyNoMoreInteractions(finalChain)
    verifyNoMoreInteractions(mockResponse)

  }
  it should "accept an Authorization header" in {
    val mockRequest = new MockHttpServletRequest("/foo")
    val mockResponse = mock[Response]
    val mockHttpResponse = mock[HttpServletResponse]
    val finalChain = mock[FilterChain]
    val mockUserProvider = mock[UserProvider]
    // Given a page that requires authorization
    val testPage = new FakeManagementPage("/foo", mockResponse, true)
    val testFilter = new ManagementFilter {
      val applicationName = "foo"
      val pages = testPage :: Nil
      val userProvider = mockUserProvider
    }
    // And a user provider for the username
    when(mockUserProvider.isValid(UserCredentials("user", "pass"))) thenReturn true
    // And a request with an authorization header
    mockRequest.addBasicAuth("user", "pass")

    // When we call doHttpFilter
    testFilter.doHttpFilter(mockRequest, mockHttpResponse, finalChain)
    // It should check with the userProvider
    verify(mockUserProvider).isValid(UserCredentials("user", "pass"))
    // It should call the page to render to the response
    verify(mockResponse).sendTo(any[ServletHttpResponse]())
    // It should not call the chain further
    verifyNoMoreInteractions(finalChain)
    verifyNoMoreInteractions(mockResponse)
    verifyNoMoreInteractions(mockUserProvider)
  }

  it should "fail the wrong password" in {
    val mockRequest = new MockHttpServletRequest("/foo")
    val mockResponse = mock[Response]
    val mockHttpResponse = mock[HttpServletResponse]
    val finalChain = mock[FilterChain]
    val mockUserProvider = mock[UserProvider]
    // Given a page that requires authorization
    val testPage = new FakeManagementPage("/foo", mockResponse, true)
    val testFilter = new ManagementFilter {
      val applicationName = "foo"
      val pages = testPage :: Nil
      val userProvider = mockUserProvider
    }
    // And a user provider for the username
    when(mockUserProvider.isValid(UserCredentials("user", "pass"))) thenReturn false
    // And a request with an authorization header
    mockRequest.addBasicAuth("user", "pass")

    // When we call doHttpFilter
    testFilter.doHttpFilter(mockRequest, mockHttpResponse, finalChain)
    // It should check with the userProvider
    verify(mockUserProvider).isValid(UserCredentials("user", "pass"))
    // It should send an error back to the user
    verify(mockHttpResponse).sendError(401, "Needs Authorisation")
    // It should not call the chain further
    verifyNoMoreInteractions(finalChain)
    verifyNoMoreInteractions(mockResponse)
    verifyNoMoreInteractions(mockUserProvider)
  }
}
