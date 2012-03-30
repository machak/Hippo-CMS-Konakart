<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="hst" uri="http://www.hippoecm.org/jsp/hst/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="html" uri="http://jakarta.apache.org/struts/tags-html" %>
<%--@elvariable id="currentCustomer" type="com.konakart.appif.CustomerIf"--%>

<hst:link var="countryDropDown" path="/restservices/checkout/states"/>


<script type="text/javascript">
    $(document).ready(function() {
        $('#CountryDropDown').change(function() {
           $.getJSON("${countryDropDown}/" + $(this).val(), "", function(data) {
                var list = $('#StateDropDown');
                list.empty('option');
                list.append($('<option />').attr('selected', 'true').text('---').val('-1'));
                $.each(data, function(index, itemData) {
                    list.append($('<option />').text(itemData.name).val(itemData.name));
                });
            });
        });
    });
</script>


 <hst:actionURL var="userAction">
    <hst:param name="action" value="${STATE}"/>
 </hst:actionURL>

<c:if test="${STATE == 'NONE'}">


<form action="${userAction}" method="post" class="form-horizontal">

    <fieldset>
        <legend><fmt:message key="onepagecheckout.title"/></legend>
        <div class="control-group <c:if test="${not empty form.message['gender']}">error</c:if>">
            <label class="control-label required"><fmt:message key="onepagecheckout.gender"/></label>

            <div class="controls">
                <label class="radio inline" for="gender-male">
                    <input type="radio" name="gender" class="input-mini" id="gender-male" value="male" <c:if test="${form.value['gender'].value == 'male'}">checked="checked"</c:if>>
                        <fmt:message key="onepagecheckout.gender.male"/>
                </label>
                <label class="radio inline" for="gender-female">
                    <input type="radio" name="gender" class="input-mini" id="gender-female" value="female" <c:if test="${form.value['gender'].value == 'female'}">checked="checked"</c:if>>
                        <fmt:message key="onepagecheckout.gender.female"/>
                </label>
                <span class="help-inline">${form.message['gender']}</span>
            </div>
        </div>
        <div class="control-group <c:if test="${not empty form.message['firstname']}">error</c:if>">
            <label class="control-label required" for="input01"><fmt:message key="onepagecheckout.firstname"/></label>

            <div class="controls">
                <input type="text" class="input-xlarge" id="input01" name="firstname" value="${form.value['firstname'].value}">
                <span class="help-inline">${form.message['firstname']}</span>
            </div>
        </div>
        <div class="control-group <c:if test="${not empty form.message['lastname']}">error</c:if>">
            <label class="control-label required" for="input02"><fmt:message key="onepagecheckout.lastname"/></label>

            <div class="controls">
                <input type="text" class="input-xlarge" id="input02" name="lastname" value="${form.value['lastname'].value}">
                <span class="help-inline">${form.message['lastname']}</span>
            </div>
        </div>
        <div class="control-group <c:if test="${not empty form.message['email']}">error</c:if>">
            <label class="control-label required" for="input03"><fmt:message key="onepagecheckout.email"/></label>

            <div class="controls">
                <input type="text" class="input-xlarge" id="input03" name="email" value="${form.value['email'].value}">
                <span class="help-inline">${form.message['email']}</span>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label required" for="input03"><fmt:message key="onepagecheckout.dateofbirth"/></label>

            <div class="controls">
                <select name="day" class="input-mini">
                    <c:forEach var="i" begin="1" end="31" step="1">
                        <option value="${i}" <c:if test="${form.value['day'].value == i}">selected="selected"</c:if>>${i}</option>
                    </c:forEach>
                </select>
                <select name="month" class="input-mini">
                    <c:forEach var="i" begin="1" end="12" step="1">
                        <option value="${i}" <c:if test="${form.value['month'].value == i}">selected="selected"</c:if>>${i}</option>
                    </c:forEach>
                </select>
                <select name="year" class="input-small">
                    <c:forEach var="i" begin="1900" end="2012" step="1">
                        <option value="${i}" <c:if test="${form.value['year'].value == i}">selected="selected"</c:if>>${i}</option>
                    </c:forEach>
                </select>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="input04"><fmt:message key="onepagecheckout.companyName"/></label>

            <div class="controls">
                <input type="text" class="input-xlarge" id="input04" name="companyname" value="${form.value['companyname'].value}">
            </div>
        </div>
        <div class="control-group <c:if test="${not empty form.message['streetaddress']}">error</c:if>">
            <label class="control-label required" for="input05"><fmt:message key="onepagecheckout.streetAddress"/></label>

            <div class="controls">
                <input type="text" class="input-xlarge" id="input05" name="streetaddress" value="${form.value['streetaddress'].value}">
                <span class="help-inline">${form.message['streetaddress']}</span>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="input06"><fmt:message key="onepagecheckout.suburb"/></label>

            <div class="controls">
                <input type="text" class="input-xlarge" id="input06" name="suburb" value="${form.value['suburb'].value}">
            </div>
        </div>
        <div class="control-group <c:if test="${not empty form.message['postalcode']}">error</c:if>">
            <label class="control-label required" for="input07"><fmt:message key="onepagecheckout.postalCode"/></label>

            <div class="controls">
                <input type="text" class="input-xlarge" id="input07" name="postalcode" value="${form.value['postalcode'].value}">
                <span class="help-inline">${form.message['postalcode']}</span>
            </div>
        </div>
        <div class="control-group <c:if test="${not empty form.message['city']}">error</c:if>">
            <label class="control-label required" for="input08"><fmt:message key="onepagecheckout.city"/></label>

            <div class="controls">
                <input type="text" class="input-xlarge" id="input08" name="city" value="${form.value['city'].value}">
                <span class="help-inline">${form.message['city']}</span>
            </div>
        </div>
        <div class="control-group <c:if test="${not empty form.message['stateprovince']}">error</c:if>">
            <label class="control-label required" for="StateDropDown"><fmt:message
                    key="onepagecheckout.stateProvince"/></label>

            <div class="controls">
                <select name="stateprovince" id="StateDropDown">
                    <option value="-1">---</option>
                    <c:forEach items="${provinces}" var="province">
                        <option value="${province}" <c:if test="${form.value['stateprovince'].value == province}">selected="selected"</c:if>>${province}</option>
                    </c:forEach>
                </select>
                <span class="help-inline">${form.message['stateprovince']}</span>
            </div>
        </div>
        <div class="control-group <c:if test="${not empty form.message['country']}">error</c:if>">
            <label class="control-label required" for="CountryDropDown"><fmt:message key="onepagecheckout.country"/></label>

            <div class="controls">
                <select name="country" id="CountryDropDown">
                    <option value="-1">---</option>
                    <c:forEach items="${countries}" var="country">
                        <option value="${country.id}" <c:if test="${form.value['country'].value == country.id}">selected="selected"</c:if>>${country.name}</option>
                    </c:forEach>
                </select>
                <span class="help-inline">${form.message['country']}</span>
            </div>
        </div>
        <div class="control-group <c:if test="${not empty form.message['primarytelephone']}">error</c:if>">
            <label class="control-label required" for="input11"><fmt:message
                    key="onepagecheckout.primaryTelephoneNumber"/></label>

            <div class="controls">
                <input type="text" class="input-xlarge" id="input11" name="primarytelephone" value="${form.value['primarytelephone'].value}">
                <span class="help-inline">${form.message['primarytelephone']}</span>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="input12"><fmt:message key="onepagecheckout.otherTelephoneNumber"/></label>

            <div class="controls">
                <input type="text" class="input-xlarge" id="input12" name="othertelephone" value="${form.value['othertelephone'].value}">
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="input13"><fmt:message key="onepagecheckout.faxNumber"/></label>

            <div class="controls">
                <input type="text" class="input-xlarge" id="input13" name="faxnumber" value="${form.value['faxnumber'].value}">
            </div>
        </div>
        <div class="form-actions">
            <hst:link var="checkout" path="checkout"/>
            <button type="submit" class="btn btn-success">Continue</button>
        </div>
    </fieldset>


</form>

</c:if>

<c:if test="${STATE == 'ASK_PASSWORD'}">

    <form action="${userAction}" method="post" class="form-horizontal">
        <input type="hidden" name="email" value="${form.value['email'].value}"/>
        <fieldset>
            <legend><fmt:message key="onepagecheckout.verification.password.title"/></legend>
            <div class="alert alert-success">
                <fmt:message key="onepagecheckout.askpassword">
                    <fmt:param value="${form.value['email'].value}"/>
                </fmt:message>
            </div>
            <div class="control-group <c:if test="${not empty form.message['password']}">error</c:if>">
                <label class="control-label required" for="input11"><fmt:message
                        key="onepagecheckout.password"/></label>

                <div class="controls">
                    <input type="password" class="input-xlarge" id="input11" name="password" value="${form.value['password'].value}">
                    <button type="submit" class="btn btn-success">Continue</button>
                    <span class="help-inline">${form.message['password']}</span>
                </div>
            </div>

        </fieldset>
    </form>


</c:if>

<c:if test="${STATE == 'LOGGED'}">
    done
</c:if>
