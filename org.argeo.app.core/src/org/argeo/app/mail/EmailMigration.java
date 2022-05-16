package org.argeo.app.mail;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static org.argeo.app.mail.EmailUtils.describe;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.System.Logger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;

import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.URLName;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.search.HeaderTerm;
import javax.mail.util.SharedFileInputStream;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.mbox.MboxFolder;
import com.sun.mail.mbox.MboxMessage;

/** Migrates emails from one storage to the another one. */
public class EmailMigration {
	private final static Logger logger = System.getLogger(EmailMigration.class.getName());

//	private String targetBaseDir;

	private String sourceServer;
	private String sourceUsername;
	private String sourcePassword;

	private String targetServer;
	private String targetUsername;
	private String targetPassword;

	public void process() throws MessagingException, IOException {
//		Path baseDir = Paths.get(targetBaseDir).resolve(sourceUsername).resolve("mbox");

		Store sourceStore = null;
		try {
			Properties sourceProperties = System.getProperties();
			sourceProperties.setProperty("mail.store.protocol", "imaps");

			Session sourceSession = Session.getInstance(sourceProperties, null);
			// session.setDebug(true);
			sourceStore = sourceSession.getStore("imaps");
			sourceStore.connect(sourceServer, sourceUsername, sourcePassword);

			Folder defaultFolder = sourceStore.getDefaultFolder();
//			migrateFolders(baseDir, defaultFolder);

			// Always start with Inbox
//			Folder inboxFolder = sourceStore.getFolder(EmailUtils.INBOX);
//			migrateFolder(baseDir, inboxFolder);

			Properties targetProperties = System.getProperties();
			targetProperties.setProperty("mail.imap.starttls.enable", "true");
			targetProperties.setProperty("mail.imap.auth", "true");

			Session targetSession = Session.getInstance(targetProperties, null);
			// session.setDebug(true);
			Store targetStore = targetSession.getStore("imap");
			targetStore.connect(targetServer, targetUsername, targetPassword);

//			Folder targetFolder = targetStore.getFolder(EmailUtils.INBOX);
//			logger.log(DEBUG, "Source message count " + inboxFolder.getMessageCount());
//			logger.log(DEBUG, "Target message count " + targetFolder.getMessageCount());

			migrateFolders(defaultFolder, targetStore);
		} finally {
			if (sourceStore != null)
				sourceStore.close();

		}
	}

	protected void migrateFolders(Folder sourceParentFolder, Store targetStore) throws MessagingException, IOException {
		folders: for (Folder sourceFolder : sourceParentFolder.list()) {
			String sourceFolderName = sourceFolder.getName();

			String sourceFolderFullName = sourceFolder.getFullName();
			char sourceFolderSeparator = sourceParentFolder.getSeparator();
			char targetFolderSeparator = targetStore.getDefaultFolder().getSeparator();
			String targetFolderFullName = sourceFolderFullName.replace(sourceFolderSeparator, targetFolderSeparator);

			// GMail specific
			if (sourceFolderFullName.equals("[Gmail]")) {
				migrateFolders(sourceFolder, targetStore);
				continue folders;
			}
			if (sourceFolderFullName.startsWith("[Gmail]")) {
				String subFolderName = null;
				// Make it configurable
				switch (sourceFolderName) {
				case "All Mail":
				case "Important":
				case "Spam":
					continue folders;
				case "Sent Mail":
					subFolderName = "Sent";
				default:
					// does nothing
				}
				targetFolderFullName = subFolderName == null ? sourceFolder.getName() : subFolderName;
			}

			int messageCount = (sourceFolder.getType() & Folder.HOLDS_MESSAGES) != 0 ? sourceFolder.getMessageCount()
					: 0;
			boolean hasSubFolders = (sourceFolder.getType() & Folder.HOLDS_FOLDERS) != 0
					? sourceFolder.list().length != 0
					: false;
			Folder targetFolder;
			if (hasSubFolders) {// has sub-folders
				if (messageCount == 0) {
					targetFolder = targetStore.getFolder(targetFolderFullName);
					if (!targetFolder.exists()) {
						targetFolder.create(Folder.HOLDS_FOLDERS);
						logger.log(DEBUG, "Created HOLDS_FOLDERS folder " + targetFolder.getFullName());
					}
				} else {// also has messages
					Folder parentFolder = targetStore.getFolder(targetFolderFullName);
					if (!parentFolder.exists()) {
						parentFolder.create(Folder.HOLDS_FOLDERS);
						logger.log(DEBUG, "Created HOLDS_FOLDERS folder " + parentFolder.getFullName());
					}
					String miscFullName = targetFolderFullName + targetFolderSeparator + "_Misc";
					targetFolder = targetStore.getFolder(miscFullName);
					if (!targetFolder.exists()) {
						targetFolder.create(Folder.HOLDS_MESSAGES);
						logger.log(DEBUG, "Created HOLDS_MESSAGES folder " + targetFolder.getFullName());
					}
				}
			} else {// no sub-folders
				if (messageCount == 0) { // empty
					logger.log(DEBUG, "Skip empty folder " + targetFolderFullName);
					continue folders;
				}
				targetFolder = targetStore.getFolder(targetFolderFullName);
				if (!targetFolder.exists()) {
					targetFolder.create(Folder.HOLDS_MESSAGES);
					logger.log(DEBUG, "Created HOLDS_MESSAGES folder " + targetFolder.getFullName());
				}
			}

			if (messageCount != 0) {

				targetFolder.open(Folder.READ_WRITE);
				try {
					long begin = System.currentTimeMillis();
					sourceFolder.open(Folder.READ_ONLY);
					migrateFolder(sourceFolder, targetFolder);
					long duration = System.currentTimeMillis() - begin;
					logger.log(DEBUG, targetFolderFullName + " - Migration of " + messageCount + " messages took "
							+ (duration / 1000) + " s (" + (duration / messageCount) + " ms per message)");
				} finally {
					sourceFolder.close();
					targetFolder.close();
				}
			}

			// recursive
			if (hasSubFolders) {
				migrateFolders(sourceFolder, targetStore);
			}
		}
	}

	protected void migrateFoldersToFs(Path baseDir, Folder sourceFolder) throws MessagingException, IOException {
		folders: for (Folder folder : sourceFolder.list()) {
			String folderName = folder.getName();

			if ((folder.getType() & Folder.HOLDS_MESSAGES) != 0) {
				// Make it configurable
				switch (folderName) {
				case "All Mail":
				case "Important":
					continue folders;
				default:
					// doe nothing
				}
				migrateFolderToFs(baseDir, folder);
			}
			if ((folder.getType() & Folder.HOLDS_FOLDERS) != 0) {
				migrateFoldersToFs(baseDir.resolve(folder.getName()), folder);
			}
		}
	}

	protected void migrateFolderToFs(Path baseDir, Folder sourceFolder) throws MessagingException, IOException {

		String folderName = sourceFolder.getName();
		sourceFolder.open(Folder.READ_ONLY);

		Folder targetFolder = null;
		try {
			int messageCount = sourceFolder.getMessageCount();
			logger.log(DEBUG, folderName + " - Message count : " + messageCount);
			if (messageCount == 0)
				return;
//			logger.log(DEBUG, folderName + " - Unread Messages : " + sourceFolder.getUnreadMessageCount());

			boolean saveAsFiles = false;

			if (saveAsFiles) {
				Message messages[] = sourceFolder.getMessages();

				for (int i = 0; i < messages.length; ++i) {
//					logger.log(DEBUG, "MESSAGE #" + (i + 1) + ":");
					Message msg = messages[i];
//					String from = "unknown";
//					if (msg.getReplyTo().length >= 1) {
//						from = msg.getReplyTo()[0].toString();
//					} else if (msg.getFrom().length >= 1) {
//						from = msg.getFrom()[0].toString();
//					}
					String subject = msg.getSubject();
					Instant sentDate = msg.getSentDate().toInstant();
//					logger.log(DEBUG, "Saving ... " + subject + " from " + from + " (" + sentDate + ")");
					String fileName = sentDate + "  " + subject;
					Path file = baseDir.resolve(fileName);
					savePartsAsFiles(msg.getContent(), file);
				}
			} else {
				long begin = System.currentTimeMillis();
				targetFolder = openMboxTargetFolder(sourceFolder, baseDir);
				migrateFolder(sourceFolder, targetFolder);
				long duration = System.currentTimeMillis() - begin;
				logger.log(DEBUG, folderName + " - Migration of " + messageCount + " messages took " + (duration / 1000)
						+ " s (" + (duration / messageCount) + " ms per message)");
			}
		} finally {
			sourceFolder.close();
			if (targetFolder != null)
				targetFolder.close();
		}
	}

	protected Folder migrateFolder(Folder sourceFolder, Folder targetFolder) throws MessagingException, IOException {
		String folderName = targetFolder.getName();

		int lastSourceNumber;
		int currentTargetMessageCount = targetFolder.getMessageCount();
		if (currentTargetMessageCount != 0) {
			MimeMessage lastTargetMessage = (MimeMessage) targetFolder.getMessage(currentTargetMessageCount);
			logger.log(DEBUG, folderName + " - Last target message " + describe(lastTargetMessage));
			Date lastTargetSent = lastTargetMessage.getReceivedDate();
			Message[] lastSourceMessage = sourceFolder
					.search(new HeaderTerm(EmailUtils.MESSAGE_ID, lastTargetMessage.getMessageID()));
			if (lastSourceMessage.length == 0)
				throw new IllegalStateException("No message found with message ID " + lastTargetMessage.getMessageID());
			if (lastSourceMessage.length != 1) {
				for (Message msg : lastSourceMessage) {
					logger.log(ERROR, "Message " + describe(msg));

				}
				throw new IllegalStateException(
						lastSourceMessage.length + " messages found with received date " + lastTargetSent.toInstant());
			}
			lastSourceNumber = lastSourceMessage[0].getMessageNumber();
		} else {
			lastSourceNumber = 0;
		}
		logger.log(DEBUG, folderName + " - Last source message number " + lastSourceNumber);

		int countToRetrieve = sourceFolder.getMessageCount() - lastSourceNumber;

		FetchProfile fetchProfile = new FetchProfile();
		fetchProfile.add(FetchProfile.Item.FLAGS);
		fetchProfile.add(FetchProfile.Item.ENVELOPE);
		fetchProfile.add(FetchProfile.Item.CONTENT_INFO);
		fetchProfile.add(FetchProfile.Item.SIZE);
		if (sourceFolder instanceof IMAPFolder) {
			// IMAPFolder sourceImapFolder = (IMAPFolder) sourceFolder;
			fetchProfile.add(IMAPFolder.FetchProfileItem.HEADERS);
			fetchProfile.add(IMAPFolder.FetchProfileItem.MESSAGE);
		}

		int batchSize = 100;
		int batchCount = countToRetrieve / batchSize;
		if (countToRetrieve % batchSize != 0)
			batchCount = batchCount + 1;
		// int batchCount = 2; // for testing
		for (int i = 0; i < batchCount; i++) {
			long begin = System.currentTimeMillis();

			int start = lastSourceNumber + i * batchSize + 1;
			int end = lastSourceNumber + (i + 1) * batchSize;
			if (end >= (lastSourceNumber + countToRetrieve + 1))
				end = lastSourceNumber + countToRetrieve;
			Message[] sourceMessages = sourceFolder.getMessages(start, end);
			sourceFolder.fetch(sourceMessages, fetchProfile);
			// targetFolder.appendMessages(sourceMessages);
			// sourceFolder.copyMessages(sourceMessages,targetFolder);

			copyMessages(sourceMessages, targetFolder);
//			copyMessagesToMbox(sourceMessages, targetFolder);

			String describeLast = describe(sourceMessages[sourceMessages.length - 1]);

//		if (i % 10 == 9) {
			// free memory from fetched messages
			sourceFolder.close();
			targetFolder.close();

			sourceFolder.open(Folder.READ_ONLY);
			targetFolder.open(Folder.READ_WRITE);
//			logger.log(DEBUG, "Open/close folder in order to free memory");
//		}

			long duration = System.currentTimeMillis() - begin;
			logger.log(DEBUG, folderName + " - batch " + i + " took " + (duration / 1000) + " s, "
					+ (duration / (end - start + 1)) + " ms per message. Last message " + describeLast);
		}

		return targetFolder;
	}

	protected Folder openMboxTargetFolder(Folder sourceFolder, Path baseDir) throws MessagingException, IOException {
		String folderName = sourceFolder.getName();
		if (sourceFolder.getName().equals(EmailUtils.INBOX_UPPER_CASE))
			folderName = EmailUtils.INBOX;// Inbox

		Path targetDir = baseDir;// .resolve("mbox");
		Files.createDirectories(targetDir);
		Path targetPath;
		if (((sourceFolder.getType() & Folder.HOLDS_FOLDERS) != 0) && sourceFolder.list().length != 0) {
			Path dir = targetDir.resolve(folderName);
			Files.createDirectories(dir);
			targetPath = dir.resolve("_Misc");
		} else {
			targetPath = targetDir.resolve(folderName);
		}
		if (!Files.exists(targetPath))
			Files.createFile(targetPath);
		URLName targetUrlName = new URLName("mbox:" + targetPath.toString());
		Properties targetProperties = new Properties();
		// targetProperties.setProperty("mail.mime.address.strict", "false");
		Session targetSession = Session.getDefaultInstance(targetProperties);
		Folder targetFolder = targetSession.getFolder(targetUrlName);
		targetFolder.open(Folder.READ_WRITE);

		return targetFolder;
	}

	protected void copyMessages(Message[] sourceMessages, Folder targetFolder) throws MessagingException {
		targetFolder.appendMessages(sourceMessages);
	}

	protected void copyMessagesToMbox(Message[] sourceMessages, Folder targetFolder)
			throws MessagingException, IOException {
		Message[] targetMessages = new Message[sourceMessages.length];
		for (int j = 0; j < sourceMessages.length; j++) {
			MimeMessage sourceMm = (MimeMessage) sourceMessages[j];
			InternetHeaders ih = new InternetHeaders();
			for (Enumeration<String> e = sourceMm.getAllHeaderLines(); e.hasMoreElements();) {
				ih.addHeaderLine(e.nextElement());
			}
			Path tmpFileSource = Files.createTempFile("argeo-mbox-source", ".txt");
			Path tmpFileTarget = Files.createTempFile("argeo-mbox-target", ".txt");
			Files.copy(sourceMm.getRawInputStream(), tmpFileSource, StandardCopyOption.REPLACE_EXISTING);

			// we use ISO_8859_1 because it is more robust than US_ASCII with regard to
			// missing characters
			try (BufferedReader reader = Files.newBufferedReader(tmpFileSource, StandardCharsets.ISO_8859_1);
					BufferedWriter writer = Files.newBufferedWriter(tmpFileTarget, StandardCharsets.ISO_8859_1);) {
				int lineNumber = 0;
				String line = null;
				try {
					while ((line = reader.readLine()) != null) {
						lineNumber++;
						if (line.startsWith("From ")) {
							writer.write(">" + line);
							logger.log(DEBUG,
									"Fix line " + lineNumber + " in " + EmailUtils.describe(sourceMm) + ": " + line);
						} else {
							writer.write(line);
						}
						writer.newLine();
					}
				} catch (IOException e) {
					logger.log(ERROR, "Error around line " + lineNumber + " of " + tmpFileSource);
					throw e;
				}
			}

			MboxMessage mboxMessage = new MboxMessage((MboxFolder) targetFolder, ih,
					new SharedFileInputStream(tmpFileTarget.toFile()), sourceMm.getMessageNumber(),
					EmailUtils.getUnixFrom(sourceMm), true);
			targetMessages[j] = mboxMessage;

			// clean up
			Files.delete(tmpFileSource);
			Files.delete(tmpFileTarget);
		}
		targetFolder.appendMessages(targetMessages);

	}

	/** Save body parts and attachments as plain files. */
	protected void savePartsAsFiles(Object content, Path fileBase) throws IOException, MessagingException {
		OutputStream out = null;
		InputStream in = null;
		try {
			if (content instanceof Multipart) {
				Multipart multi = ((Multipart) content);
				int parts = multi.getCount();
				for (int j = 0; j < parts; ++j) {
					MimeBodyPart part = (MimeBodyPart) multi.getBodyPart(j);
					if (part.getContent() instanceof Multipart) {
						// part-within-a-part, do some recursion...
						savePartsAsFiles(part.getContent(), fileBase);
					} else {
						String extension = "";
						if (part.isMimeType("text/html")) {
							extension = "html";
						} else {
							if (part.isMimeType("text/plain")) {
								extension = "txt";
							} else {
								// Try to get the name of the attachment
								extension = part.getDataHandler().getName();
							}
						}
						String filename = fileBase + "." + extension;
						System.out.println("... " + filename);
						out = new FileOutputStream(new File(filename));
						in = part.getInputStream();
						int k;
						while ((k = in.read()) != -1) {
							out.write(k);
						}
					}
				}
			}
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.flush();
				out.close();
			}
		}
	}

	public void setSourceServer(String sourceServer) {
		this.sourceServer = sourceServer;
	}

	public void setSourceUsername(String sourceUsername) {
		this.sourceUsername = sourceUsername;
	}

	public void setSourcePassword(String sourcePassword) {
		this.sourcePassword = sourcePassword;
	}

	public void setTargetServer(String targetServer) {
		this.targetServer = targetServer;
	}

	public void setTargetUsername(String targetUsername) {
		this.targetUsername = targetUsername;
	}

	public void setTargetPassword(String targetPassword) {
		this.targetPassword = targetPassword;
	}

	public static void main(String args[]) throws Exception {
		if (args.length < 6)
			throw new IllegalArgumentException(
					"usage: <source IMAP server> <source username> <source password> <target IMAP server> <target username> <target password>");
		String sourceServer = args[0];
		String sourceUsername = args[1];
		String sourcePassword = args[2];
		String targetServer = args[3];
		String targetUsername = args[4];
		String targetPassword = args[5];

		EmailMigration emailMigration = new EmailMigration();
		emailMigration.setSourceServer(sourceServer);
		emailMigration.setSourceUsername(sourceUsername);
		emailMigration.setSourcePassword(sourcePassword);
		emailMigration.setTargetServer(targetServer);
		emailMigration.setTargetUsername(targetUsername);
		emailMigration.setTargetPassword(targetPassword);

		emailMigration.process();
	}
}
