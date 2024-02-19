package swisst.core;

import org.apache.logging.log4j.message.Message;

class MsgFactory {

	public static Message getMessage(String format, Object... args) {

		return new Message() {
			/**
			 *
			 */
			private static final long serialVersionUID = 1L;
			private Object[] a = args;
			private String f = format;
			@Override
			public String getFormattedMessage() {

				return String.format(f, a);
			}

			@Override
			public String getFormat() {
				return getFormattedMessage();
			}

			@Override
			public Object[] getParameters() {
				return args;
			}

			@Override
			public Throwable getThrowable() {
				return null;
			}

		};
	}

}