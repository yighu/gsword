class FeedJob {
  def cronExpression = "0 0 0, * * ?"
  def feedService
  def group = "GSword"

  def execute() {
    feedService.membiblefeed()
    feedService.gswordfeed()
  }

}
