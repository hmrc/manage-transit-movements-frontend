package views.departure.drafts

import play.api.data.Form
import play.twirl.api.HtmlFormat
import views.behaviours.YesNoViewBehaviours
import views.html.departure.drafts.DeleteDraftDepartureYesNoView

class DeleteDraftDepartureYesNoViewSpec extends YesNoViewBehaviours {

  override def applyView(form: Form[Boolean]): HtmlFormat.Appendable =
    injector.instanceOf[DeleteDraftDepartureYesNoView].apply(form)(fakeRequest, messages)

  override val prefix: String = "departure.drafts.deleteDraftDepartureYesNo"

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithRadioItems()

  behave like pageWithSubmitButton("Save and continue")
}
