package edu.newhaven.virtualfarmersmarket

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.bouncycastle.jce.provider.BouncyCastleProvider
import java.security.Security
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

object Mailer {

    init {
        Security.addProvider(BouncyCastleProvider())
    }

    private fun sendMessageTo(emailFrom: String, session: Session, message: String, subject: String, emailTo: String) {
        session.debug = true
        try {
            MimeMessage(session).let { mime ->
                mime.setFrom(InternetAddress(emailFrom))
                mime.addRecipient(Message.RecipientType.TO, InternetAddress(emailTo))
                mime.subject = subject
                mime.setText(message)
                //mime.setContent(message, "text/html")
                Transport.send(mime)
            }

        } catch (e: MessagingException) {
            Log.i("MessagingException", e.toString()) // Or use timber, it really doesn't matter
        }
    }

    fun sendMailToSeller(emailToName: String, sellerEmail: String, buyerEmail: String, buyerPhone: String, product: String) {

        val props = Properties()
        props["mail.smtp.host"] = "smtp.gmail.com"
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.port"] = "465"
        props["mail.smtp.ssl.enable"] = true
        props["mail.smtp.auth"] = "true"
        props["mail.user"] = "virtualfarmersmarketdev@gmail.com"
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.ssl.trust"] = "smtp.gmail.com"
        props["mail.mime.charset"] = "UTF-8"

        // Open a session
        val session = Session.getDefaultInstance(props, object : javax.mail.Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication("virtualfarmersmarketdev@gmail.com", "mani@margaret99")
            }
        })

        // Create a message
        val message = "Dear ${emailToName}, \n\n" +
                "Greetings from VIRTUAL FARMERS MARKET !!\n\n" +
                "You have a buyer that is interested to buy $product from your list.\n " +
                "Below is the contact information:\n"+
                "   Email ID: $buyerEmail\n" +
                "   Phone: $buyerPhone\n" + "Contact the buyer at your earliest convenience.\n" +
                "\nNOTE: Remember, when you contact potential buyers to include the Virtual " +
                "Farmer's Market in the the subject line.\n" +
                "If the product is no longer available, please mark it sold or delete in the app. " +
                "Thank you for listing your products for sale in the Virtual Farmer's Market!"

        // Create subject
        val subject = "VFM: A buyer wants your $product"

        // Send Email
        CoroutineScope(Dispatchers.IO).launch { sendMessageTo("virtualfarmersmarketdev@gmail.com", session, message, subject, sellerEmail) }
    }

    fun sendMailToBuyer(emailToName: String, buyerEmail: String, product: String) {

        val props = Properties()
        props["mail.smtp.host"] = "smtp.gmail.com"
        props["mail.smtp.auth"] = "true"
        props["mail.smtp.port"] = "465"
        props["mail.smtp.ssl.enable"] = true
        props["mail.smtp.auth"] = "true"
        props["mail.user"] = "virtualfarmersmarketdev@gmail.com"
        props["mail.smtp.starttls.enable"] = "true"
        props["mail.smtp.ssl.trust"] = "smtp.gmail.com"
        props["mail.mime.charset"] = "UTF-8"

        // Open a session
        val session = Session.getDefaultInstance(props, object : javax.mail.Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication("virtualfarmersmarketdev@gmail.com", "mani@margaret99")
            }
        })

        // Create a message
        val message = "Dear ${emailToName}, \n\n" +
                "Greetings from VIRTUAL FARMERS MARKET !!\n\n" +
                "You have showed in buying $product.\n" +
                "You will be contacted by the seller soon.\n\n" +
                "Thanks & Regards,\n Virtual Farmers Market."

        // Create subject
        val subject = "VFM: you requested to buy $product"

        // Send Email
        CoroutineScope(Dispatchers.IO).launch { sendMessageTo("virtualfarmersmarketdev@gmail.com", session, message, subject, buyerEmail) }
    }

}