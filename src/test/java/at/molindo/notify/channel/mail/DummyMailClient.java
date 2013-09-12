package at.molindo.notify.channel.mail;

import at.molindo.notify.model.Dispatch;

public class DummyMailClient implements IMailClient {

	@Override
	public void send(Dispatch dispatch) throws MailException {
		// do nothing
	}

}
