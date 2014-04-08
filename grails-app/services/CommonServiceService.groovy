import java.text.SimpleDateFormat;
class CommonServiceService {

    boolean transactional = false

    def today() {
      def dt = new Date();
      java.text.DateFormat dateFormat = new SimpleDateFormat("MMdd")
      dateFormat.format(dt)
    } 

}
