package org.argeo.suite.workbench.commands;

import static org.argeo.connect.util.JxlUtils.getStringValue;
import static org.argeo.eclipse.ui.EclipseUiUtils.notEmpty;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.activities.ActivitiesNames;
import org.argeo.connect.ConnectConstants;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.resources.ResourcesNames;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.JxlUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.people.ContactValueCatalogs;
import org.argeo.people.PeopleException;
import org.argeo.people.PeopleNames;
import org.argeo.people.PeopleService;
import org.argeo.people.PeopleTypes;
import org.argeo.people.util.PeopleJcrUtils;
import org.argeo.people.util.PersonJcrUtils;
import org.argeo.suite.SuiteException;
import org.argeo.suite.workbench.AsUiPlugin;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.rap.fileupload.FileDetails;
import org.eclipse.rap.fileupload.FileUploadEvent;
import org.eclipse.rap.fileupload.FileUploadHandler;
import org.eclipse.rap.fileupload.FileUploadListener;
import org.eclipse.rap.fileupload.FileUploadReceiver;
import org.eclipse.rap.rwt.service.ServerPushSession;
import org.eclipse.rap.rwt.widgets.FileUpload;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

import jxl.Sheet;
import jxl.Workbook;

/** Open a one page wizard to import an EXCEL 2003 legacy organisation file */
public class ImportEntities extends AbstractHandler implements PeopleNames {
	private final static Log log = LogFactory.getLog(ImportEntities.class);

	public final static String ID = AsUiPlugin.PLUGIN_ID + ".importEntities";

	// public final static String PARAM_NODE_TYPE = "param.nodeType";

	private static final Map<String, String> KNOWN_TEMPLATES;
	static {
		Map<String, String> tmpMap = new HashMap<String, String>();
		tmpMap.put("Organisations", PeopleTypes.PEOPLE_ORG);
		tmpMap.put("Persons", PeopleTypes.PEOPLE_PERSON);
		KNOWN_TEMPLATES = Collections.unmodifiableMap(tmpMap);
	}

	// TODO make this configurable
	private final static String IMPORT_ENCODING = "ISO-8859-1";// "UTF-8";

	/* DEPENDENCY INJECTION */
	private Repository repository;
	private ResourcesService resourcesService;
	private PeopleService peopleService;

	public Object execute(final ExecutionEvent event) throws ExecutionException {
		// String jcrId = event.getParameter(PARAM_NODE_TYPE);

		Wizard wizard = new ImportMappingFileWizard(HandlerUtil.getActiveShell(event),
				"Upload legacy contact via Excel file import");
		WizardDialog dialog = new WizardDialog(HandlerUtil.getActiveShell(event), wizard);
		dialog.open();
		return null;
	}

	/** One page wizard to import a EXCEL 2003 Mapping files */
	private class ImportMappingFileWizard extends Wizard {

		// Various UI Objects
		private UserInputPage userInputPage;
		private Combo resourceTypeCombo;

		// File upload
		private FileUpload fileUpload;
		private Label fileNameLabel;
		private ServerPushSession pushSession;
		private File file;

		public ImportMappingFileWizard(Shell parentShell, String title) {
			setWindowTitle(title);
		}

		@Override
		public void addPages() {
			try {
				userInputPage = new UserInputPage("User input page");
				addPage(userInputPage);
			} catch (Exception e) {
				throw new SuiteException("Cannot add page to wizard", e);
			}
		}

		/** Performs the real import. */
		@Override
		public boolean performFinish() {
			String templateName = resourceTypeCombo.getItem(resourceTypeCombo.getSelectionIndex());
			String type = KNOWN_TEMPLATES.get(templateName);
			InputStream in = null;
			try {
				in = new FileInputStream(file);
				if (PeopleTypes.PEOPLE_ORG.equals(type))
					importDefaultOrgFile(in);
				else if (PeopleTypes.PEOPLE_PERSON.equals(type))
					importDefaultPersonFile(in);
			} catch (IOException e) {
				throw new SuiteException("Cannot import mapping file", e);
			} finally {
				IOUtils.closeQuietly(in);
			}
			return true;
		}

		@Override
		public boolean performCancel() {
			return true;
		}

		@Override
		public boolean canFinish() {
			if (resourceTypeCombo.getSelectionIndex() < 0) {
				userInputPage.setErrorMessage("Please choose an entity type");
				return false;
			} else if (file == null) {
				userInputPage.setErrorMessage("Please upload a file");
				return false;
			} else {
				userInputPage.setErrorMessage(null);
				return true;
			}
		}

		private class UserInputPage extends WizardPage {
			private static final long serialVersionUID = 1L;

			public UserInputPage(String pageName) {
				super(pageName);
				setTitle("Upload an Excel 2003 file (.xls)");
			}

			public void createControl(Composite parent) {
				parent.setLayout(new GridLayout(1, false));
				Composite composite = new Composite(parent, SWT.NONE);
				composite.setLayout(new GridLayout(2, false));
				composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

				// Import type
				resourceTypeCombo = createLC(composite, "Type");
				resourceTypeCombo.addModifyListener(new ModifyListener() {
					private static final long serialVersionUID = 1L;

					@Override
					public void modifyText(ModifyEvent event) {
						getWizard().getContainer().updateButtons();
					}
				});
				resourceTypeCombo.setItems(KNOWN_TEMPLATES.keySet().toArray(new String[0]));
				resourceTypeCombo.select(0);

				// File upload
				Label lbl = new Label(composite, SWT.NONE);
				lbl.setText("Chosen file");
				lbl.setFont(EclipseUiUtils.getBoldFont(composite));
				Composite uploadCmp = new Composite(composite, SWT.NONE);
				uploadCmp.setLayoutData(EclipseUiUtils.fillWidth());
				createFileUploadArea(uploadCmp);
				setControl(composite);
			}
		}

		private Control createFileUploadArea(Composite parent) {
			GridLayout gl = EclipseUiUtils.noSpaceGridLayout(new GridLayout(2, false));
			gl.horizontalSpacing = 5;
			parent.setLayout(gl);

			fileNameLabel = new Label(parent, SWT.NONE | SWT.BEGINNING);
			fileNameLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			fileUpload = new FileUpload(parent, SWT.NONE);
			fileUpload.setText("Browse...");
			fileUpload.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

			final String url = startUploadReceiver();
			pushSession = new ServerPushSession();

			fileUpload.addSelectionListener(new SelectionAdapter() {
				private static final long serialVersionUID = 1L;

				@Override
				public void widgetSelected(SelectionEvent e) {
					String fileName = fileUpload.getFileName();
					fileNameLabel.setText(fileName == null ? "" : fileName);
					pushSession.start();
					fileUpload.submit(url);
				}
			});
			return parent;
		}

		private String startUploadReceiver() {
			MyFileUploadReceiver receiver = new MyFileUploadReceiver();
			FileUploadHandler uploadHandler = new FileUploadHandler(receiver);
			uploadHandler.addUploadListener(new FileUploadListener() {

				public void uploadProgress(FileUploadEvent event) {
					// handle upload progress
				}

				public void uploadFailed(FileUploadEvent event) {
					ImportMappingFileWizard.this.userInputPage
							.setErrorMessage("upload failed: " + event.getException());
				}

				public void uploadFinished(FileUploadEvent event) {
					fileNameLabel.getDisplay().asyncExec(new Runnable() {
						public void run() {
							ImportMappingFileWizard.this.getContainer().updateButtons();
							pushSession.stop();
						}
					});
				}
			});
			return uploadHandler.getUploadUrl();
		}

		private class MyFileUploadReceiver extends FileUploadReceiver {

			private static final String TEMP_FILE_PREFIX = "fileupload_";

			@Override
			public void receive(InputStream dataStream, FileDetails details) throws IOException {
				File result = File.createTempFile(TEMP_FILE_PREFIX, "");
				FileOutputStream outputStream = new FileOutputStream(result);
				try {
					copy(dataStream, outputStream);
				} finally {
					dataStream.close();
					outputStream.close();
				}
				if (file != null)
					file.delete();
				file = result;
			}
		}

		private void copy(InputStream inputStream, OutputStream outputStream) throws IOException {
			byte[] buffer = new byte[8192];
			boolean finished = false;
			while (!finished) {
				int bytesRead = inputStream.read(buffer);
				if (bytesRead != -1) {
					outputStream.write(buffer, 0, bytesRead);
				} else {
					finished = true;
				}
			}
		}

		/** Creates label and Combo. */
		protected Combo createLC(Composite parent, String label) {
			Label lbl = new Label(parent, SWT.RIGHT);
			lbl.setText(label);
			lbl.setFont(EclipseUiUtils.getBoldFont(parent));
			lbl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
			Combo combo = new Combo(parent, SWT.READ_ONLY);
			combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			return combo;
		}
	}

	private Node importDefaultOrgFile(InputStream in) throws IOException {
		// TODO make this configurable
		int displayNameIndex = 0;
		int legalNameIndex = 1;
		int legalFormIndex = 2;
		int urlsIndex = 3;
		int streetIndex = 4;
		int postalCodeIndex = 5;
		int lIndex = 6;
		int stIndex = 7;
		int cIndex = 8;
		int mobileIndex = 9;
		int telephoneNumberIndex = 10;
		int mailIndex = 11;
		int contactsIndex = 12;
		int descriptionIndex = 13;
		int tagsIndex = 14;

		Session session = null;
		int i = 0;
		try {
			Workbook wb = JxlUtils.toWorkbook(in, IMPORT_ENCODING);
			session = repository.login();
			String basePath = "/" + peopleService.getBaseRelPath(PeopleTypes.PEOPLE_ORG);
			Node targetParent = session.getNode(basePath);
			Sheet sheet = wb.getSheet(0);

			Node tmpParent = peopleService.getDraftParent(session);

			int rowNb = sheet.getRows();
			for (i = 1; i < rowNb - 1; i++) {

				Node tmpOrg = createDraftNode(tmpParent, PeopleTypes.PEOPLE_ORG);

				String dName = JxlUtils.getStringValue(sheet, displayNameIndex, i);
				if (notEmpty(dName))
					tmpOrg.setProperty(PeopleNames.PEOPLE_DISPLAY_NAME, dName);
				String lName = getStringValue(sheet, legalNameIndex, i);
				if (notEmpty(lName))
					tmpOrg.setProperty(PeopleNames.PEOPLE_LEGAL_NAME, lName);
				String lForm = getStringValue(sheet, legalFormIndex, i);
				if (notEmpty(lForm))
					tmpOrg.setProperty(PeopleNames.PEOPLE_LEGAL_FORM, lForm);
				String urlStr = getStringValue(sheet, urlsIndex, i);
				if (notEmpty(urlStr))
					importUrls(tmpOrg, urlStr);
				String mailStr = getStringValue(sheet, mailIndex, i);
				if (notEmpty(mailStr))
					importMails(tmpOrg, mailStr);
				String streetStr = getStringValue(sheet, streetIndex, i);
				String pcStr = getStringValue(sheet, postalCodeIndex, i);
				String lStr = getStringValue(sheet, lIndex, i);
				String stStr = getStringValue(sheet, stIndex, i);
				String cStr = getStringValue(sheet, cIndex, i);
				if (notEmpty(streetStr) || notEmpty(pcStr) || notEmpty(lStr) || notEmpty(stStr) || notEmpty(cStr))
					PeopleJcrUtils.createAddress(resourcesService, peopleService, tmpOrg, streetStr, null, pcStr, lStr,
							stStr, cStr, true, ContactValueCatalogs.CONTACT_CAT_MAIN, null);
				String mobileStr = getStringValue(sheet, mobileIndex, i);
				if (notEmpty(mobileStr))
					PeopleJcrUtils.createPhone(resourcesService, peopleService, tmpOrg, mobileStr, true, null, null);
				String phoneStr = getStringValue(sheet, telephoneNumberIndex, i);
				if (notEmpty(phoneStr))
					PeopleJcrUtils.createPhone(resourcesService, peopleService, tmpOrg, phoneStr, true,
							ContactValueCatalogs.CONTACT_CAT_DIRECT, null);
				String descStr = getStringValue(sheet, descriptionIndex, i);
				if (notEmpty(descStr))
					tmpOrg.setProperty(Property.JCR_DESCRIPTION, descStr);
				String tagsStr = getStringValue(sheet, tagsIndex, i);
				if (notEmpty(tagsStr))
					tmpOrg.setProperty(ResourcesNames.CONNECT_TAGS, ConnectJcrUtils.parseAndClean(tagsStr, ",", true));

				Node newOrgNode = peopleService.publishEntity(targetParent, PeopleTypes.PEOPLE_ORG, tmpOrg);
				// Save the newly created entity without creating a base version
				newOrgNode = peopleService.saveEntity(newOrgNode, false);

				String contactsStr = getStringValue(sheet, contactsIndex, i);
				if (notEmpty(contactsStr))
					importOrgEmployees(tmpParent, targetParent, newOrgNode, contactsStr);
			}

			// Refresh tags and mailing list
			Node tagParent = resourcesService.getTagLikeResourceParent(session, ConnectConstants.RESOURCE_TAG);
			resourcesService.refreshKnownTags(tagParent);

			// Create Mailing lists
			Node mlParent = resourcesService.getTagLikeResourceParent(session, PeopleTypes.PEOPLE_MAILING_LIST);
			resourcesService.refreshKnownTags(mlParent);

		} catch (PeopleException | RepositoryException e) {
			throw new SuiteException("Cannot import mapping file, error at line: " + (i + 1), e);
		} finally {
			JcrUtils.logoutQuietly(session);
		}

		return null;
	}

	private Map<String, Integer> initialiseHeaders(Sheet sheet, List<String> validHeaders) {
		Map<String, Integer> headers = new HashMap<>();
		int length = sheet.getColumns();
		for (int i = 0; i < length; i++) {
			String value = JxlUtils.getStringValue(sheet, i, 0);
			if (validHeaders.contains(value))
				headers.put(value, i);
			else
				log.warn(value + " (column [" + i + "]) is not a valid header");
		}
		return headers;
	}

	private Node importDefaultPersonFile(InputStream in) throws IOException {
		// Local shortcut
		String JCR_DESC = ConnectJcrUtils.getLocalJcrItemName(Property.JCR_DESCRIPTION);

		// Map<String, Integer> headers = new HashMap<>();
		// headers.put(PeopleNames.PEOPLE_FIRST_NAME, 0);
		// headers.put(PEOPLE_LAST_NAME, 1);
		// headers.put(PEOPLE_SALUTATION, 2);
		// headers.put(PEOPLE_HONORIFIC_TITLE, 3);
		// headers.put(PEOPLE_NICKNAME, 4);
		// headers.put(PEOPLE_NAME_SUFFIX, 5);
		// headers.put(PEOPLE_MAIDEN_NAME, 6);
		// headers.put(PEOPLE_PMOBILE, 7);
		// headers.put(PEOPLE_PTELEPHONE_NUMBER, 8);
		// headers.put(PEOPLE_SPOKEN_LANGUAGES, 9);
		// headers.put(ResourcesNames.CONNECT_TAGS, 10);
		// headers.put(PEOPLE_MAILING_LISTS, 11);
		// headers.put(PEOPLE_BIRTH_DATE, 12);
		// headers.put(PEOPLE_PMAIL, 13);
		// headers.put("people:emailAddressOther", 14);
		// headers.put("people:org", 15);
		// headers.put(PEOPLE_ROLE, 16);
		// headers.put(PEOPLE_DEPARTMENT, 17);
		// headers.put("people:facebook", 18);
		// headers.put(JCR_DESC, 19);
		// headers.put(ActivitiesNames.ACTIVITIES_FROM_IP, 20);

		List<String> validHeaders = Arrays.asList(PeopleNames.PEOPLE_FIRST_NAME, PEOPLE_LAST_NAME, PEOPLE_SALUTATION,
				PEOPLE_HONORIFIC_TITLE, PEOPLE_NICKNAME, PEOPLE_NAME_SUFFIX, PEOPLE_MAIDEN_NAME, PEOPLE_PMOBILE,
				PEOPLE_PTELEPHONE_NUMBER, PEOPLE_SPOKEN_LANGUAGES, ResourcesNames.CONNECT_TAGS, PEOPLE_MAILING_LISTS,
				PEOPLE_BIRTH_DATE, PEOPLE_PMAIL, "people:emailAddressOther", "people:org", PEOPLE_ROLE,
				PEOPLE_DEPARTMENT, "people:facebook", JCR_DESC, ActivitiesNames.ACTIVITIES_FROM_IP, PEOPLE_STREET,
				PEOPLE_ZIP_CODE, PEOPLE_CITY, PEOPLE_STATE, PEOPLE_COUNTRY);

		String[] basicInfo = { PEOPLE_FIRST_NAME, PEOPLE_LAST_NAME, PEOPLE_SALUTATION, PEOPLE_HONORIFIC_TITLE,
				PEOPLE_NICKNAME, PEOPLE_NAME_SUFFIX, PEOPLE_MAIDEN_NAME, PEOPLE_BIRTH_DATE, JCR_DESC };

		Map<String, String> primaryContacts = new HashMap<>();
		primaryContacts.put(PEOPLE_PMOBILE, PeopleTypes.PEOPLE_MOBILE);
		primaryContacts.put(PEOPLE_PTELEPHONE_NUMBER, PeopleTypes.PEOPLE_TELEPHONE_NUMBER);
		primaryContacts.put(PEOPLE_PURL, PeopleTypes.PEOPLE_URL);
		primaryContacts.put(PEOPLE_PMAIL, PeopleTypes.PEOPLE_MAIL);

		String[] multiProps = { PEOPLE_SPOKEN_LANGUAGES, ResourcesNames.CONNECT_TAGS, PEOPLE_MAILING_LISTS, };

		Session session = null;
		int i = 0;
		try {
			Workbook wb = JxlUtils.toWorkbook(in, IMPORT_ENCODING);
			session = repository.login();
			String basePath = "/" + peopleService.getBaseRelPath(PeopleTypes.PEOPLE_PERSON);
			Node targetParent = session.getNode(basePath);
			Sheet sheet = wb.getSheet(0);

			Map<String, Integer> headers = initialiseHeaders(sheet, validHeaders);

			Node tmpParent = peopleService.getDraftParent(session);

			int rowNb = sheet.getRows();
			for (i = 1; i < rowNb; i++) {
				Node tmpPerson = createDraftNode(tmpParent, PeopleTypes.PEOPLE_PERSON);

				for (String propName : basicInfo) {
					if (headers.containsKey(propName)) {
						String value = JxlUtils.getStringValue(sheet, headers.get(propName), i);
						if (notEmpty(value))
							tmpPerson.setProperty(propName, value);
					}
				}

				for (String propName : multiProps) {
					if (headers.containsKey(propName)) {
						String value = JxlUtils.getStringValue(sheet, headers.get(propName), i);
						if (notEmpty(value))
							tmpPerson.setProperty(propName, ConnectJcrUtils.parseAndClean(value, ",", true));
					}
				}

				for (String propName : primaryContacts.keySet()) {
					if (headers.containsKey(propName)) {
						String value = JxlUtils.getStringValue(sheet, headers.get(propName), i);
						if (notEmpty(value)) {
							PeopleJcrUtils.createContact(resourcesService, peopleService, tmpPerson,
									primaryContacts.get(propName), value, true,
									ContactValueCatalogs.CONTACT_CAT_MAIN, null);
						}
					}
				}

				// Specific values
				String propName = "people:emailAddressOther";
				if (headers.containsKey(propName)) {
					String value = JxlUtils.getStringValue(sheet, headers.get(propName), i);
					if (notEmpty(value)) {
						PeopleJcrUtils.createContact(resourcesService, peopleService, tmpPerson,
								PeopleTypes.PEOPLE_MAIL, value, false, ContactValueCatalogs.CONTACT_CAT_MAIN,
								null);

					}
				}

				propName = "people:facebook";
				if (headers.containsKey(propName)) {
					String value = JxlUtils.getStringValue(sheet, headers.get(propName), i);
					if (notEmpty(value)) {
						PeopleJcrUtils.createContact(resourcesService, peopleService, tmpPerson,
								PeopleTypes.PEOPLE_SOCIAL_MEDIA, value, false,
								ContactValueCatalogs.CONTACT_CAT_FACEBOOK, null);
					}
				}

				String street = null, zip = null, city = null, state = null, country = null;
				if (headers.containsKey(PEOPLE_STREET))
					street = JxlUtils.getStringValue(sheet, headers.get(PEOPLE_STREET), i);
				if (headers.containsKey(PEOPLE_ZIP_CODE))
					zip = JxlUtils.getStringValue(sheet, headers.get(PEOPLE_ZIP_CODE), i);
				if (headers.containsKey(PEOPLE_CITY))
					city = JxlUtils.getStringValue(sheet, headers.get(PEOPLE_CITY), i);
				if (headers.containsKey(PEOPLE_STATE))
					state = JxlUtils.getStringValue(sheet, headers.get(PEOPLE_STATE), i);
				if (headers.containsKey(PEOPLE_COUNTRY))
					country = JxlUtils.getStringValue(sheet, headers.get(PEOPLE_COUNTRY), i);

				if (notEmpty(street) || notEmpty(zip) || notEmpty(city) || notEmpty(state) || notEmpty(country)) {
					PeopleJcrUtils.createAddress(resourcesService, peopleService, tmpPerson, street, null, zip, city,
							state, country, true, ContactValueCatalogs.CONTACT_CAT_MAIN, null);
				}

				// TODO Experimental connection activity to store info about the
				// IP from where a person has registered himself in the system
				// propName = ActivitiesNames.ACTIVITIES_FROM_IP;

				// TODO create a job and possibly the related organisation

				Node newPersonNode = peopleService.publishEntity(targetParent, PeopleTypes.PEOPLE_PERSON, tmpPerson);
				// Save the newly created entity without creating a base version
				newPersonNode = peopleService.saveEntity(newPersonNode, false);
			}

			// Refresh tags and mailing list
			Node tagParent = resourcesService.getTagLikeResourceParent(session, ConnectConstants.RESOURCE_TAG);
			resourcesService.refreshKnownTags(tagParent);

			// Create Mailing lists
			Node mlParent = resourcesService.getTagLikeResourceParent(session, PeopleTypes.PEOPLE_MAILING_LIST);
			resourcesService.refreshKnownTags(mlParent);
		} catch (PeopleException | RepositoryException e) {
			String message = "Cannot import mapping file, error at line: " + (i + 1);
			// TODO Check why the error is swallowed.
			log.error(message, e);
			throw new SuiteException(message, e);
		} finally {
			JcrUtils.logoutQuietly(session);
		}
		return null;
	}

	private Node createDraftNode(Node parent, String mainMixin) throws RepositoryException {
		String uuid = UUID.randomUUID().toString();
		Node tmpNode = parent.addNode(uuid);
		tmpNode.addMixin(mainMixin);
		tmpNode.setProperty(ConnectNames.CONNECT_UID, uuid);
		return tmpNode;
	}

	private void importOrgEmployees(Node tmpParent, Node targetParent, Node newOrgNode, String coworkersStr)
			throws RepositoryException {
		String[] coworkers = coworkersStr.split("\\n");
		loop: for (String line : coworkers) {
			if (EclipseUiUtils.isEmpty(line))
				continue loop;
			line = line.trim();
			int index = line.indexOf(' ');
			String firstName = null;
			String lastName = null;
			String position = null;
			if (index == -1)
				firstName = line;
			else {
				firstName = line.substring(0, index);
				line = line.substring(index);

				index = line.indexOf('(');
				if (index == -1)
					lastName = line;
				else {
					lastName = line.substring(0, index).trim();
					position = line.substring(index + 1, line.length() - 1);
				}
			}
			Node tmpPerson = createDraftNode(tmpParent, PeopleTypes.PEOPLE_PERSON);
			tmpPerson.setProperty(PEOPLE_FIRST_NAME, firstName);
			if (EclipseUiUtils.notEmpty(lastName))
				tmpPerson.setProperty(PEOPLE_LAST_NAME, lastName);
			Node newPersonNode = peopleService.publishEntity(targetParent, PeopleTypes.PEOPLE_PERSON, tmpPerson);
			// if (EclipseUiUtils.notEmpty(position))
			PersonJcrUtils.addJob(resourcesService, peopleService, newPersonNode, newOrgNode, position, true);
			// Save the newly created entity without creating a base version
			newPersonNode = peopleService.saveEntity(newPersonNode, false);

		}
	}

	private void importUrls(Node contactable, String urlStr) throws RepositoryException {
		String[] urls = urlStr.split("\\n");
		boolean hasPrimary = false;
		boolean hasPrimaryFacebook = false;

		loop: for (String line : urls) {
			if (EclipseUiUtils.isEmpty(line))
				continue loop;
			line = line.trim();

			if (line.startsWith("https://www.facebook.com")) {
				PeopleJcrUtils.createSocialMedia(resourcesService, peopleService, contactable, line,
						!hasPrimaryFacebook, ContactValueCatalogs.CONTACT_CAT_FACEBOOK, null);
				hasPrimaryFacebook = true;
			} else {
				PeopleJcrUtils.createWebsite(resourcesService, peopleService, contactable, line, !hasPrimary, null);
				hasPrimary = true;
			}
		}
	}

	private void importMails(Node contactable, String mailStr) throws RepositoryException {
		String[] urls = mailStr.split("\\n");
		boolean hasPrimary = false;
		loop: for (String line : urls) {
			if (EclipseUiUtils.isEmpty(line))
				continue loop;
			line = line.trim();
			PeopleJcrUtils.createEmail(resourcesService, peopleService, contactable, line, !hasPrimary, null, null);
			hasPrimary = true;
		}
	}

	/* DEPENDENCY INJECTION */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setResourcesService(ResourcesService resourcesService) {
		this.resourcesService = resourcesService;
	}

	public void setPeopleService(PeopleService peopleService) {
		this.peopleService = peopleService;
	}
}
