class MembbJob {
  def cronExpression = "0 0 5 * * ?"
  def twitterService
  def group = "GSword"

  def execute() {
    twitterService.membible()
  }

}
