package requests.skelethon.requesters;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import lombok.Builder;
import models.BaseModel;
import requests.skelethon.Endpoint;
import requests.skelethon.HttpRequest;
import requests.skelethon.interfaces.CrudEndpointInterface;

public class ValidatedCrudRequester <T extends BaseModel> extends HttpRequest implements CrudEndpointInterface {
    private CrudRequester crudRequester;
    public ValidatedCrudRequester(Endpoint endpoint, RequestSpecification requestSpecification, ResponseSpecification responseSpecification) {
        super(endpoint, requestSpecification, responseSpecification);
        this.crudRequester = new CrudRequester(endpoint, requestSpecification, responseSpecification);
    }

    @Override
    public T post(BaseModel model) {

        return (T) crudRequester.post(model)
                .extract()
                .as(endpoint.getResponseModel());
    }

    @Override
    public T get(Long id) {

        return (T) crudRequester.get(id)
                .extract()
                .as(endpoint.getResponseModel());
    }

    @Override
    public T getWithoutId() {
        return (T) crudRequester.getWithoutId()
                .extract()
                .as(endpoint.getResponseModel());
    }

    @Override
    public T update(BaseModel model) {

        return (T) crudRequester.update(model)
                .extract()
                .as(endpoint.getResponseModel());
    }

    @Override
    public Object delete(Long id) {
        return null;
    }
}
