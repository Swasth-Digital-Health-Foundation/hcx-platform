package io.hcxprotocol.createresource;

import ca.uhn.fhir.model.api.annotation.*;
import ca.uhn.fhir.model.api.annotation.Extension;
import ca.uhn.fhir.util.ElementUtil;
import org.hl7.fhir.r4.model.*;

import java.util.Collections;
import java.util.UUID;

@ResourceDef(name="InsurancePlan", profile="https://ig.hcxprotocol.io/v0.7/StructureDefinition-HCXInsurancePlan.html")
public class HCXInsurancePlan extends InsurancePlan {

    public HCXInsurancePlan() {}

    public HCXInsurancePlan(String identifier, Enumerations.PublicationStatus status, String name) {
        this.setId(UUID.randomUUID().toString());
        Meta meta = new Meta();
        meta.setProfile(Collections.singletonList(new CanonicalType("https://ig.hcxprotocol.io/v0.7/StructureDefinition-HCXInsurancePlan.html")));
        this.setMeta(meta);
        this.getIdentifier().add(new Identifier().setValue(identifier));
        this.setStatus(status);
        this.setType(Collections.singletonList(new CodeableConcept().setCoding(Collections.singletonList(new Coding().setCode("medical").setSystem("http://terminology.hl7.org/CodeSystem/insurance-plan-type")))));
        this.setName(name);
    }

    @Block
    public static class InsurancePlanPlanComponent extends InsurancePlan.InsurancePlanPlanComponent {

        public InsurancePlanPlanComponent() {
            this.setId(UUID.randomUUID().toString());
        }

        @Child(name="HCXProofOfIdentificationExtension")
        @Extension(url="https://ig.hcxprotocol.io/v0.7/StructureDefinition-HCXProofOfIdentificationExtension.html", definedLocally=false, isModifier=false)
        @Description(shortDefinition="Adding Proof of Identification as extension to base InsuracncePlan SD")
        protected IdentificationExtension identificationExtension;

        @Child(name="HCXProofOfPresenceExtension")
        @Extension(url="https://ig.hcxprotocol.io/v0.7/StructureDefinition-HCXProofOfPresenceExtension.html", definedLocally=false, isModifier=false)
        @Description(shortDefinition="Adding Proof of Presence as extension to base InsuracncePlan SD")
        protected PresenceExtension presenceExtension;

        public IdentificationExtension getIdentificationExtension() {
            return identificationExtension;
        }

        public void setIdentificationExtension(IdentificationExtension identificationExtension) {
            this.identificationExtension = identificationExtension;
        }

        public PresenceExtension getPresenceExtension() {
            return presenceExtension;
        }

        public void setPresenceExtension(PresenceExtension presenceExtension) {
            this.presenceExtension = presenceExtension;
        }

        public void setType(String code) {
            // TODO: another parameter for system path?
            // TODO: setCoding is a list. Do we need to support more parameters?
            this.setType(new CodeableConcept().setCoding(Collections.singletonList(new Coding().setCode(code).setSystem("http://terminologyServer/ValueSets/plan-type"))));
        }

        @Override
        public boolean isEmpty() {
            return super.isEmpty() && ElementUtil.isEmpty(identificationExtension,presenceExtension);
        }
    }




    @Block
    public static class IdentificationExtension extends BackboneElement {

        private static final long serialVersionUID = 4522090347756045145L;

        @Child(name = "proofOfIdDocumentCode")
        @Extension(url = "ProofOfIdentificationDocumentCode", definedLocally = false, isModifier = false)
        private CodeableConcept proofOfIdDocumentCode;

        @Child(name = "proofOfIdentificationDocumentRequiredFlag")
        @Extension(url = "ProofOfIdentificationDocumentRequiredFlag", definedLocally = false, isModifier = false)
        private BooleanType proofOfIdentificationDocumentRequiredFlag;

        @Child(name = "proofOfIdentificationDocumentMimeType")
        @Extension(url = "ProofOfIdentificationDocumentMimeType", definedLocally = false, isModifier = false)
        private CodeType proofOfIdentificationDocumentMimeType;

        @Child(name = "clinicalDiagnosticDocumentClaimUse")
        @Extension(url = "ClinicalDiagnosticDocumentClaimUse", definedLocally = false, isModifier = false)
        private CodeType clinicalDiagnosticDocumentClaimUse;

        @Child(name = "documentationUrl")
        @Extension(url = "DocumentationUrl", definedLocally = false, isModifier = false)
        private UrlType documentationUrl;

        public IdentificationExtension() {
            this.setDocumentationUrl(new UrlType("http://documentation-url"));
        }

        public CodeableConcept getProofOfIdDocumentCode() {
            return proofOfIdDocumentCode;
        }

        public void setProofOfIdDocumentCode(CodeableConcept proofOfIdDocumentCode) {
            this.proofOfIdDocumentCode = proofOfIdDocumentCode;
        }

        public BooleanType isProofOfIdentificationDocumentRequiredFlag() {
            return proofOfIdentificationDocumentRequiredFlag;
        }

        public void setProofOfIdentificationDocumentRequiredFlag(BooleanType proofOfIdentificationDocumentRequiredFlag) {
            this.proofOfIdentificationDocumentRequiredFlag = proofOfIdentificationDocumentRequiredFlag;
        }

        public CodeType getProofOfIdentificationDocumentMimeType() {
            return proofOfIdentificationDocumentMimeType;
        }

        public void setProofOfIdentificationDocumentMimeType(CodeType proofOfIdentificationDocumentMimeType) {
            this.proofOfIdentificationDocumentMimeType = proofOfIdentificationDocumentMimeType;
        }

        public CodeType getClinicalDiagnosticDocumentClaimUse() {
            return clinicalDiagnosticDocumentClaimUse;
        }

        public void setClinicalDiagnosticDocumentClaimUse(CodeType clinicalDiagnosticDocumentClaimUse) {
            this.clinicalDiagnosticDocumentClaimUse = clinicalDiagnosticDocumentClaimUse;
        }

        public UrlType getDocumentationUrl() {
            return documentationUrl;
        }

        public void setDocumentationUrl(UrlType documentationUrl) {
            this.documentationUrl = documentationUrl;
        }

        public void setProofOfIdDocumentCode(String code, String display) {
            this.setProofOfIdDocumentCode(new CodeableConcept(new Coding().setSystem("https://hcx-valuesets/proofOfIdentificationDocumentCodes").setCode(code).setDisplay(display).setVersion("1.0.0")));
        }

        public void setClinicalDiagnosticDocumentClaimUse(String claimUseCode) {
            this.setClinicalDiagnosticDocumentClaimUse(new CodeType(claimUseCode));
        }

        @Override
        public IdentificationExtension copy() {
            IdentificationExtension copy = new IdentificationExtension();
            copy().clinicalDiagnosticDocumentClaimUse =  clinicalDiagnosticDocumentClaimUse;
            copy().proofOfIdentificationDocumentMimeType = proofOfIdentificationDocumentMimeType;
            copy().proofOfIdentificationDocumentRequiredFlag = proofOfIdentificationDocumentRequiredFlag;
            copy().proofOfIdDocumentCode = proofOfIdDocumentCode;
            copy().documentationUrl = documentationUrl;
            return copy;
        }

        @Override
        public boolean isEmpty() {
            return super.isEmpty() && ElementUtil.isEmpty(clinicalDiagnosticDocumentClaimUse,proofOfIdDocumentCode,proofOfIdentificationDocumentMimeType,proofOfIdentificationDocumentRequiredFlag,documentationUrl);
        }
    }

    @Block
    public static class PresenceExtension extends BackboneElement {

        private static final long serialVersionUID = 4522090347756045146L;

        public PresenceExtension() {
            this.setDocumentationUrl(new UrlType("http://documentation-url"));
        }

        @Child(name = "proofOfPresenceDocumentCode")
        @Extension(url = "ProofOfPresenceDocumentCode", definedLocally = false, isModifier = false)
        private CodeableConcept proofOfPresenceDocumentCode;

        @Child(name = "proofOfPresenceDocumentRequiredFlag")
        @Extension(url = "ProofOfPresenceDocumentRequiredFlag", definedLocally = false, isModifier = false)
        private BooleanType proofOfPresenceDocumentRequiredFlag;

        @Child(name = "proofOfPresenceDocumentMimeType")
        @Extension(url = "ProofOfPresenceDocumentMimeType", definedLocally = false, isModifier = false)
        private CodeType proofOfPresenceDocumentMimeType;

        @Child(name = "clinicalDiagnosticDocumentClaimUse")
        @Extension(url = "ClinicalDiagnosticDocumentClaimUse", definedLocally = false, isModifier = false)
        private CodeType clinicalDiagnosticDocumentClaimUse;

        @Child(name = "documentationUrl")
        @Extension(url = "DocumentationUrl", definedLocally = false, isModifier = false)
        private UrlType documentationUrl;

        public CodeableConcept getProofOfPresenceDocumentCode() {
            return proofOfPresenceDocumentCode;
        }

        public void setProofOfPresenceDocumentCode(CodeableConcept proofOfPresenceDocumentCode) {
            this.proofOfPresenceDocumentCode = proofOfPresenceDocumentCode;
        }

        public BooleanType isProofOfPresenceDocumentRequiredFlag() {
            return proofOfPresenceDocumentRequiredFlag;
        }

        public void setProofOfPresenceDocumentRequiredFlag(BooleanType proofOfPresenceDocumentRequiredFlag) {
            this.proofOfPresenceDocumentRequiredFlag = proofOfPresenceDocumentRequiredFlag;
        }

        public CodeType getProofOfPresenceDocumentMimeType() {
            return proofOfPresenceDocumentMimeType;
        }

        public void setProofOfPresenceDocumentMimeType(CodeType proofOfPresenceDocumentMimeType) {
            this.proofOfPresenceDocumentMimeType = proofOfPresenceDocumentMimeType;
        }

        public CodeType getClinicalDiagnosticDocumentClaimUse() {
            return clinicalDiagnosticDocumentClaimUse;
        }

        public void setClinicalDiagnosticDocumentClaimUse(CodeType clinicalDiagnosticDocumentClaimUse) {
            this.clinicalDiagnosticDocumentClaimUse = clinicalDiagnosticDocumentClaimUse;
        }

        public UrlType getDocumentationUrl() {
            return documentationUrl;
        }

        public void setDocumentationUrl(UrlType documentationUrl) {
            this.documentationUrl = documentationUrl;
        }

        public void setClinicalDiagnosticDocumentClaimUse(String claimUseCode) {
            this.setClinicalDiagnosticDocumentClaimUse(new CodeType(claimUseCode));
        }

        public void setProofOfPresenceDocumentCode(String code, String display) {
            this.setProofOfPresenceDocumentCode(new CodeableConcept(new Coding().setSystem("https://hcx-valuesets/proofOfIdentificationDocumentCodes").setCode(code).setDisplay(display).setVersion("1.0.0")));
        }

        @Override
        public PresenceExtension copy() {
            PresenceExtension copy = new PresenceExtension();
            copy().clinicalDiagnosticDocumentClaimUse =  clinicalDiagnosticDocumentClaimUse;
            copy().proofOfPresenceDocumentCode =  proofOfPresenceDocumentCode;
            copy().proofOfPresenceDocumentMimeType = proofOfPresenceDocumentMimeType;
            copy().proofOfPresenceDocumentRequiredFlag = proofOfPresenceDocumentRequiredFlag;
            copy().documentationUrl = documentationUrl;
            return copy;
        }
        @Override
        public boolean isEmpty() {
            return super.isEmpty() && ElementUtil.isEmpty(clinicalDiagnosticDocumentClaimUse,proofOfPresenceDocumentCode, proofOfPresenceDocumentMimeType,proofOfPresenceDocumentRequiredFlag,documentationUrl);
        }
    }


    //Extensions for benefits

    @Block
    public static class PlanBenefitComponent extends InsurancePlan.PlanBenefitComponent{

        public DiagnosticDocumentsExtension getDiagnosticDocumentsExtension() {
            return diagnosticDocumentsExtension;
        }

        public void setDiagnosticDocumentsExtension(DiagnosticDocumentsExtension diagnosticDocumentsExtension) {
            this.diagnosticDocumentsExtension = diagnosticDocumentsExtension;
        }

        @Child(name="HCXDiagnosticDocumentsExtension")
        @Extension(url="https://ig.hcxprotocol.io/v0.7/StructureDefinition-HCXDiagnosticDocumentsExtension.html", definedLocally=false, isModifier=false)
        @Description(shortDefinition="List of documents to be submitted to claim the benefit.")
        protected DiagnosticDocumentsExtension diagnosticDocumentsExtension;

        public InformationalMessagesExtension getInformationalMessagesExtension() {
            return informationalMessagesExtension;
        }

        public void setInformationalMessagesExtension(InformationalMessagesExtension informationalMessagesExtension) {
            this.informationalMessagesExtension = informationalMessagesExtension;
        }

        @Child(name="HCXInformationalMessagesExtension")
        @Extension(url="https://ig.hcxprotocol.io/v0.7/StructureDefinition-HCXInformationalMessagesExtension.html", definedLocally=false, isModifier=false)
        @Description(shortDefinition="\tMessages related to the benefit.")
        protected InformationalMessagesExtension informationalMessagesExtension;

        public QuestionnairesExtension getQuestionnairesExtension() {
            return questionnairesExtension;
        }

        public void setQuestionnairesExtension(QuestionnairesExtension questionnairesExtension) {
            this.questionnairesExtension = questionnairesExtension;
        }

        @Child(name="HCXQuestionnairesExtension")
        @Extension(url="https://ig.hcxprotocol.io/v0.7/StructureDefinition-HCXQuestionnairesExtension.html", definedLocally=false, isModifier=false)
        @Description(shortDefinition="Questionnaires to be answered to claim the benefit.")
        protected QuestionnairesExtension questionnairesExtension;

        @Override
        public boolean isEmpty() {
            return super.isEmpty() && ElementUtil.isEmpty(informationalMessagesExtension,questionnairesExtension,diagnosticDocumentsExtension);
        }

    }

    @Block
    public static class DiagnosticDocumentsExtension extends BackboneElement {

        private static final long serialVersionUID = 4522090347756045146L;

        public CodeableConcept getClinicalDiagnosticDocumentCode() {
            return clinicalDiagnosticDocumentCode;
        }

        public void setClinicalDiagnosticDocumentCode(CodeableConcept clinicalDiagnosticDocumentCode) {
            this.clinicalDiagnosticDocumentCode = clinicalDiagnosticDocumentCode;
        }

        @Child(name = "clinicalDiagnosticDocumentCode")
        @Extension(url = "ClinicalDiagnosticDocumentCode", definedLocally = false, isModifier = false)
        private CodeableConcept clinicalDiagnosticDocumentCode;

        public BooleanType getClinicalDiagnosticDocumentRequiredFlag() {
            return clinicalDiagnosticDocumentRequiredFlag;
        }

        public void setClinicalDiagnosticDocumentRequiredFlag(BooleanType clinicalDiagnosticDocumentRequiredFlag) {
            this.clinicalDiagnosticDocumentRequiredFlag = clinicalDiagnosticDocumentRequiredFlag;
        }

        @Child(name = "clinicalDiagnosticDocumentRequiredFlag")
        @Extension(url = "clinicalDiagnosticDocumentRequiredFlag", definedLocally = false, isModifier = false)
        private BooleanType clinicalDiagnosticDocumentRequiredFlag;

        public CodeType getClinicalDiagnosticDocumentMimeType() {
            return clinicalDiagnosticDocumentMimeType;
        }

        public void setClinicalDiagnosticDocumentMimeType(CodeType clinicalDiagnosticDocumentMimeType) {
            this.clinicalDiagnosticDocumentMimeType = clinicalDiagnosticDocumentMimeType;
        }

        @Child(name = "clinicalDiagnosticDocumentMimeType")
        @Extension(url = "ClinicalDiagnosticDocumentMimeType", definedLocally = false, isModifier = false)
        private CodeType clinicalDiagnosticDocumentMimeType;

        public CodeType getClinicalDiagnosticDocumentClaimUse() {
            return clinicalDiagnosticDocumentClaimUse;
        }

        public void setClinicalDiagnosticDocumentClaimUse(CodeType clinicalDiagnosticDocumentClaimUse) {
            this.clinicalDiagnosticDocumentClaimUse = clinicalDiagnosticDocumentClaimUse;
        }

        @Child(name = "clinicalDiagnosticDocumentClaimUse")
        @Extension(url = "ClinicalDiagnosticDocumentClaimUse", definedLocally = false, isModifier = false)
        private CodeType clinicalDiagnosticDocumentClaimUse;

        public UrlType getDocumentationUrl() {
            return documentationUrl;
        }

        public void setDocumentationUrl(UrlType documentationUrl) {
            this.documentationUrl = documentationUrl;
        }

        @Child(name = "documentationUrl")
        @Extension(url = "DocumentationUrl", definedLocally = false, isModifier = false)
        private UrlType documentationUrl;

        @Override
        public DiagnosticDocumentsExtension  copy() {
            DiagnosticDocumentsExtension copy = new DiagnosticDocumentsExtension();
            copy().clinicalDiagnosticDocumentCode = clinicalDiagnosticDocumentCode;
            copy().clinicalDiagnosticDocumentRequiredFlag =clinicalDiagnosticDocumentRequiredFlag;
            copy().clinicalDiagnosticDocumentMimeType = clinicalDiagnosticDocumentMimeType;
            copy().clinicalDiagnosticDocumentClaimUse =  clinicalDiagnosticDocumentClaimUse;
            copy().documentationUrl = documentationUrl;
            return copy;
        }
        @Override
        public boolean isEmpty() {
            return super.isEmpty() && ElementUtil.isEmpty(clinicalDiagnosticDocumentClaimUse,clinicalDiagnosticDocumentCode,clinicalDiagnosticDocumentMimeType,clinicalDiagnosticDocumentRequiredFlag,documentationUrl);
        }
    }

    @Block
    public static class InformationalMessagesExtension extends BackboneElement {

        private static final long serialVersionUID = 4522090347756045147L;


        public CodeableConcept getInformationalMessagesCode() {
            return informationalMessagesCode;
        }

        public void setInformationalMessagesCode(CodeableConcept informationalMessagesCode) {
            this.informationalMessagesCode = informationalMessagesCode;
        }

        @Child(name = "informationalMessagesCode")
        @Extension(url = "InformationalMessagesCode", definedLocally = false, isModifier = false)
        private CodeableConcept informationalMessagesCode;


        public BooleanType getInformationalMessageCode() {
            return informationalMessageCode;
        }

        public void setInformationalMessageCode(BooleanType informationalMessageCode) {
            this.informationalMessageCode = informationalMessageCode;
        }

        @Child(name = "informationalMessageCode")
        @Extension(url = "InformationalMessageCode", definedLocally = false, isModifier = false)
        private BooleanType informationalMessageCode;


        public CodeType getInformationalMessageClaimUse() {
            return informationalMessageClaimUse;
        }

        public void setInformationalMessageClaimUse(CodeType informationalMessageClaimUse) {
            this.informationalMessageClaimUse = informationalMessageClaimUse;
        }

        @Child(name = "informationalMessageClaimUse")
        @Extension(url = "InformationalMessageClaimUse", definedLocally = false, isModifier = false)
        private CodeType informationalMessageClaimUse;

        public UrlType getDocumentationUrl() {
            return documentationUrl;
        }

        public void setDocumentationUrl(UrlType documentationUrl) {
            this.documentationUrl = documentationUrl;
        }

        @Child(name = "documentationUrl")
        @Extension(url = "DocumentationUrl", definedLocally = false, isModifier = false)
        private UrlType documentationUrl;

        @Override
        public InformationalMessagesExtension  copy() {
            InformationalMessagesExtension copy = new InformationalMessagesExtension();
            copy().documentationUrl = documentationUrl;
            copy().informationalMessageClaimUse = informationalMessageClaimUse;
            copy().informationalMessagesCode = informationalMessagesCode;
            copy().informationalMessageCode = informationalMessageCode;
            return copy;
        }
        @Override
        public boolean isEmpty() {
            return super.isEmpty() && ElementUtil.isEmpty(informationalMessageClaimUse,informationalMessageCode,informationalMessagesCode,documentationUrl);
        }
    }

    @Block
    public static class QuestionnairesExtension extends BackboneElement {

        private static final long serialVersionUID = 4522090347756045148L;


        public Reference getQuestionnaire() {
            return questionnaire;
        }

        public void setQuestionnaire(Reference questionnaire) {
            this.questionnaire = questionnaire;
        }

        @Child(name = "questionnaire")
        @Extension(url = "Questionnaire", definedLocally = false, isModifier = false)
        private Reference questionnaire;


        public BooleanType getQuestionnaireRequiredFlag() {
            return questionnaireRequiredFlag;
        }

        public void setQuestionnaireRequiredFlag(BooleanType questionnaireRequiredFlag) {
            this.questionnaireRequiredFlag = questionnaireRequiredFlag;
        }

        @Child(name = "questionnaireRequiredFlag")
        @Extension(url = "QuestionnaireRequiredFlag", definedLocally = false, isModifier = false)
        private BooleanType questionnaireRequiredFlag;


        public CodeType getQuestionnaireClaimUse() {
            return questionnaireClaimUse;
        }

        public void setQuestionnaireClaimUse(CodeType questionnaireClaimUse) {
            this.questionnaireClaimUse = questionnaireClaimUse;
        }

        @Child(name = "questionnaireClaimUse")
        @Extension(url = "QuestionnaireClaimUse", definedLocally = false, isModifier = false)
        private CodeType questionnaireClaimUse;

        public UrlType getDocumentationUrl() {
            return documentationUrl;
        }

        public void setDocumentationUrl(UrlType documentationUrl) {
            this.documentationUrl = documentationUrl;
        }

        @Child(name = "documentationUrl")
        @Extension(url = "DocumentationUrl", definedLocally = false, isModifier = false)
        private UrlType documentationUrl;

        @Override
        public QuestionnairesExtension  copy() {
            QuestionnairesExtension copy = new QuestionnairesExtension();
            copy().documentationUrl = documentationUrl;
            copy().questionnaire = questionnaire;
            copy().questionnaireClaimUse = questionnaireClaimUse;
            copy().questionnaireRequiredFlag = questionnaireRequiredFlag;
            return copy;
        }
        @Override
        public boolean isEmpty() {
            return super.isEmpty() && ElementUtil.isEmpty(questionnaire,questionnaireClaimUse,questionnaireRequiredFlag,documentationUrl);
        }
    }
}
