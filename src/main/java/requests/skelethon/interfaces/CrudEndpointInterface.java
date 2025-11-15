package requests.skelethon.interfaces;

import models.BaseModel;

public interface CrudEndpointInterface {

    Object post(BaseModel model);
    Object get(Long id);
    Object getWithoutId();
    Object update(BaseModel model);
    Object delete(Long id);
}
