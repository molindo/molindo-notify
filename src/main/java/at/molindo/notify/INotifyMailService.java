package at.molindo.notify;

import at.molindo.notify.INotifyService.NotifyException;
import at.molindo.notify.model.Notification;

public interface INotifyMailService {
	void mailNow(String recipient, Notification notification) throws NotifyException;
}
