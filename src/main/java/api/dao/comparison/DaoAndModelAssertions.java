package api.dao.comparison;

import api.dao.BaseDaoModel;
import api.models.BaseModel;
import api.dao.UserDao;
import api.dao.AccountDao;
import org.assertj.core.api.AbstractAssert;

public class DaoAndModelAssertions {

    private static final DaoComparator daoComparator = new DaoComparator();

    public static DaoModelAssert assertThat(BaseModel dtoModel, BaseDaoModel daoModel) {
        return new DaoModelAssert(dtoModel, daoModel);
    }

    public static class DaoModelAssert extends AbstractAssert<DaoModelAssert, Object> {
        private final BaseModel dtoModel;
        private final BaseDaoModel daoModel;

        public DaoModelAssert(BaseModel dtoModel, BaseDaoModel daoModel) {
            super(dtoModel, DaoModelAssert.class);
            this.dtoModel = dtoModel;
            this.daoModel = daoModel;
        }

        public DaoModelAssert match() {
            if (dtoModel == null) {
                failWithMessage("DTO model should not be null");
            }

            if (daoModel == null) {
                failWithMessage("DAO model should not be null");
            }

            // Use configurable comparison
            try {
                daoComparator.compare(dtoModel, daoModel);
            } catch (AssertionError e) {
                failWithMessage(e.getMessage());
            }

            return this;
        }
    }
}