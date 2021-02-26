package de.vitagroup.num.interceptors;

import ca.uhn.fhir.rest.api.RequestTypeEnum;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.rest.server.interceptor.consent.IConsentContextServices;
import ca.uhn.fhir.rest.server.interceptor.consent.IConsentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.openehealth.ipf.commons.audit.AuditContext;
import org.openehealth.ipf.commons.audit.codes.EventActionCode;
import org.openehealth.ipf.commons.audit.codes.EventOutcomeIndicator;
import org.openehealth.ipf.commons.audit.event.PatientRecordBuilder;
import org.openehealth.ipf.commons.audit.model.ActiveParticipantType;
import org.openehealth.ipf.commons.audit.model.AuditMessage;
import org.openehealth.ipf.commons.audit.types.EventType;
import org.springframework.security.core.context.SecurityContextHolder;

@Slf4j
@RequiredArgsConstructor
public class AuditInterceptor implements IConsentService {

  private static final String SYSTEM_NAME = "CTR";

  private static final String CREATE_TEXT = "create";

  private static final String UPDATE_TEXT = "update";

  private static final String DELETE_TEXT = "delete";

  private static final String READ_TEXT = "read";

  private static final String HL7_CODING_SYSTEM =
    "http://terminology.hl7.org/CodeSystem/audit-event-type";

  private static final String PATIENT_PATH_PREFIX = "Patient/";

  private static final String SUCCESS_LOG_MESSAGE =
    "{}: {} request for {} executed successfully by userid {}";

  private static final String FAILURE_LOG_MESSAGE =
    "{}: {} request for {} executed with failure by userid {}";

  private final AuditContext auditContext;

  @Override
  public void completeOperationSuccess(
    RequestDetails theRequestDetails, IConsentContextServices theContextServices) {
    String userId = SecurityContextHolder.getContext().getAuthentication().getName();

    if (RequestTypeEnum.GET.equals(theRequestDetails.getRequestType())) {
      String requestPath = theRequestDetails.getRequestPath();
      if (requestPath.startsWith(PATIENT_PATH_PREFIX)) {
        IdType patientId = new IdType(requestPath);
        validateAndSend(
          patientSuccessAuditMessage(
            EventOutcomeIndicator.Success,
            userId,
            patientId.getIdPart(),
            StringUtils.EMPTY,
            "Patient read successfully",
            EventActionCode.Read,
            READ_TEXT));
      } else {
        log.info(SUCCESS_LOG_MESSAGE, SYSTEM_NAME, READ_TEXT, requestPath, userId);
      }
    } else if (RequestTypeEnum.PUT.equals(theRequestDetails.getRequestType())) {
      if (theRequestDetails.getResource().getClass().isAssignableFrom(Patient.class)) {
        Patient resource = (Patient) theRequestDetails.getResource();
        validateAndSend(
          patientSuccessAuditMessage(
            EventOutcomeIndicator.Success,
            userId,
            resource.getId(),
            resource.getName().get(0).getNameAsSingleString(),
            "Patient updated successfully",
            EventActionCode.Read,
            UPDATE_TEXT));
      } else {
        log.info(
          SUCCESS_LOG_MESSAGE,
          SYSTEM_NAME,
          UPDATE_TEXT,
          theRequestDetails.getResource().getIdElement().getValue(),
          userId);
      }
    } else if (RequestTypeEnum.DELETE.equals(theRequestDetails.getRequestType())) {
      String requestPath = theRequestDetails.getRequestPath();
      if (requestPath.startsWith(PATIENT_PATH_PREFIX)) {
        IdType patientId = new IdType(requestPath);
        validateAndSend(
          patientSuccessAuditMessage(
            EventOutcomeIndicator.Success,
            userId,
            patientId.getIdPart(),
            StringUtils.EMPTY,
            "Patient deleted successfully",
            EventActionCode.Delete,
            DELETE_TEXT));
      } else {
        log.info(
          SUCCESS_LOG_MESSAGE,
          SYSTEM_NAME,
          DELETE_TEXT,
          theRequestDetails.getResource().getIdElement().getValue(),
          userId);
      }
    } else if (RequestTypeEnum.POST.equals(theRequestDetails.getRequestType())) {
      if (theRequestDetails.getResource().getClass().isAssignableFrom(Patient.class)) {
        Patient resource = (Patient) theRequestDetails.getResource();
        validateAndSend(
          patientSuccessAuditMessage(
            EventOutcomeIndicator.Success,
            userId,
            resource.getId(),
            resource.getName().get(0).getNameAsSingleString(),
            "Patient created successfully",
            EventActionCode.Create,
            CREATE_TEXT));
      } else {
        log.info(
          SUCCESS_LOG_MESSAGE,
          SYSTEM_NAME,
          CREATE_TEXT,
          theRequestDetails.getResource().getIdElement().getValue(),
          userId);
      }
    }
  }

  @Override
  public void completeOperationFailure(
    RequestDetails theRequestDetails,
    BaseServerResponseException theException,
    IConsentContextServices theContextServices) {

    String userId = SecurityContextHolder.getContext().getAuthentication().getName();

    if (RequestTypeEnum.GET.equals(theRequestDetails.getRequestType())) {
      String requestPath = theRequestDetails.getRequestPath();
      if (requestPath.startsWith(PATIENT_PATH_PREFIX)) {
        IdType patientId = new IdType(requestPath);
        validateAndSend(
          patientSuccessAuditMessage(
            EventOutcomeIndicator.MajorFailure,
            userId,
            patientId.getIdPart(),
            StringUtils.EMPTY,
            "Patient read failed",
            EventActionCode.Read,
            READ_TEXT));
      } else {
        log.info(FAILURE_LOG_MESSAGE, SYSTEM_NAME, READ_TEXT, requestPath, userId, theException);
      }
    } else if (RequestTypeEnum.PUT.equals(theRequestDetails.getRequestType())) {
      if (theRequestDetails.getResource().getClass().isAssignableFrom(Patient.class)) {
        Patient resource = (Patient) theRequestDetails.getResource();
        validateAndSend(
          patientSuccessAuditMessage(
            EventOutcomeIndicator.MajorFailure,
            userId,
            resource.getId(),
            resource.getName().get(0).getNameAsSingleString(),
            "Patient update failed",
            EventActionCode.Read,
            UPDATE_TEXT));
      } else {
        log.info(
          FAILURE_LOG_MESSAGE,
          SYSTEM_NAME,
          UPDATE_TEXT,
          theRequestDetails.getResource().getIdElement().getValue(),
          userId,
          theException);
      }
    } else if (RequestTypeEnum.DELETE.equals(theRequestDetails.getRequestType())) {
      String requestPath = theRequestDetails.getRequestPath();
      if (requestPath.startsWith(PATIENT_PATH_PREFIX)) {
        IdType patientId = new IdType(requestPath);
        validateAndSend(
          patientSuccessAuditMessage(
            EventOutcomeIndicator.MajorFailure,
            userId,
            patientId.getIdPart(),
            StringUtils.EMPTY,
            "Patient deletion failed",
            EventActionCode.Delete,
            DELETE_TEXT));
      } else {
        log.info(
          FAILURE_LOG_MESSAGE,
          SYSTEM_NAME,
          DELETE_TEXT,
          theRequestDetails.getResource().getIdElement().getValue(),
          userId,
          theException);
      }
    } else if (RequestTypeEnum.POST.equals(theRequestDetails.getRequestType())) {
      if (theRequestDetails.getResource().getClass().isAssignableFrom(Patient.class)) {
        Patient resource = (Patient) theRequestDetails.getResource();
        validateAndSend(
          patientSuccessAuditMessage(
            EventOutcomeIndicator.MajorFailure,
            userId,
            resource.getId(),
            resource.getName().get(0).getNameAsSingleString(),
            "Patient create failed",
            EventActionCode.Create,
            CREATE_TEXT));
      } else {
        log.info(
          FAILURE_LOG_MESSAGE,
          SYSTEM_NAME,
          CREATE_TEXT,
          theRequestDetails.getResource().getIdElement().getValue(),
          userId,
          theException);
      }
    }
  }

  private AuditMessage patientSuccessAuditMessage(
    EventOutcomeIndicator outcomeIndicator,
    String userId,
    String patientId,
    String patientName,
    String description,
    EventActionCode eventActionCode,
    String eventText) {
    return new PatientRecordBuilder(
      outcomeIndicator,
      description,
      eventActionCode,
      EventType.of(eventText, HL7_CODING_SYSTEM, eventText))
      .addPatient(patientId, patientName, null)
      .addActiveParticipant(new ActiveParticipantType(userId, true))
      .setAuditSource(auditContext)
      .getMessage();
  }

  private void validateAndSend(AuditMessage auditMessage) {
    auditMessage.validate();
    auditContext.audit(auditMessage);
  }
}
