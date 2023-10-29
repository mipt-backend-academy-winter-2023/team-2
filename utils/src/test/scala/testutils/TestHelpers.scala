package testutils

import zio.http.Response
import zio.http.model.Status
import zio.test.{TestResult, assertTrue}

object TestHelpers {
  def assertOk(response: Response): TestResult =
    assertTrue(response.status == Status.Ok)

  def assertBadRequest(response: Response): TestResult =
    assertTrue(response.status == Status.BadRequest)

  def assertNotFound(response: zio.http.Response): TestResult =
    assertTrue(response.status == Status.NotFound)
}
