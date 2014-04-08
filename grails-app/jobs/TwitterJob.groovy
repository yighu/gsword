class TwitterJob {
  def cronExpression = "0 0 5,12,16,21 * * ?"
  def twitterService
  def group = "GSword"

  def execute() {
    twitterService.update()
  }

}
