package gov.nist.hit.hl7.codeset.adapter.model.phinvads;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Document(collection = "phinvads-valueset")
public class PhinvadsValueset {
    @Id
    private String id;
    @CreatedDate
    private Date creationDate;
    private Date updateDate;
    private String bindingIdentifier;
    private String oid;
    private String url;
    private int numberOfCodes;
    private Set<String> codeSystems = new HashSet<String>();
    private Set<PhinvadsCode> codes = new HashSet<PhinvadsCode>();
    private int version;
    private String comment;
    private String description;
    private String name;
    private String preDef;
    private String postDef;
    private String scope;
    private Boolean hasPartCodes;

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getNumberOfCodes() {
        return numberOfCodes;
    }

    public void setNumberOfCodes(int numberOfCodes) {
        this.numberOfCodes = numberOfCodes;
    }

    public Set<String> getCodeSystems() {
        return codeSystems;
    }

    public void setCodeSystems(Set<String> codeSystems) {
        this.codeSystems = codeSystems;
    }

    public Set<PhinvadsCode> getCodes() {
        return codes;
    }

    public void setCodes(Set<PhinvadsCode> codes) {
        this.codes = codes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Date getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
        this.updateDate = updateDate;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBindingIdentifier() {
        return bindingIdentifier;
    }

    public void setBindingIdentifier(String bindingIdentifier) {
        this.bindingIdentifier = bindingIdentifier;
    }

    public String getPreDef() {
        return preDef;
    }

    public void setPreDef(String preDef) {
        this.preDef = preDef;
    }

    public String getPostDef() {
        return postDef;
    }

    public void setPostDef(String postDef) {
        this.postDef = postDef;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Boolean getHasPartCodes() {
        return hasPartCodes;
    }

    public void setHasPartCodes(Boolean hasPartCodes) {
        this.hasPartCodes = hasPartCodes;
    }


}

