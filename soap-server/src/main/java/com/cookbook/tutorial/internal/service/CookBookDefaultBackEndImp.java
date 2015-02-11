package com.cookbook.tutorial.internal.service;

import com.cookbook.tutorial.internal.dsql.DsqlParser;
import com.cookbook.tutorial.service.*;
import org.apache.cxf.common.util.StringUtils;
import org.parboiled.Parboiled;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;
import org.springframework.util.CollectionUtils;

import javax.jws.WebParam;
import java.util.*;

/**
 * Created by Mulesoft.
 *
 * This is a dummy implementation, none persistent. Will be valid until the server stops running.
 *
 */
public class CookBookDefaultBackEndImp implements IMuleCookBookService {

    private Integer currentIndex = 0;

    public CookBookDefaultBackEndImp(){
        initialize();
    }

    private void initialize() {

        try {
            Ingredient ingredient =  new Ingredient();
            ingredient.setName("Apple");
            ingredient.setUnit(UnitType.UNIT);
            this.create(ingredient);

            Recipe recipe = new Recipe();
            recipe.setName("Apple Pie");
            List<Ingredient> ingredients = new ArrayList<Ingredient>();
            ingredients.add(ingredient);
            recipe.setIngredients(ingredients);
            this.create(recipe);

        } catch (SessionExpiredException e) {
            e.printStackTrace();
        } catch (InvalidEntityException e) {
            e.printStackTrace();
        }
    }

    private Map<Integer, CookBookEntity> entities = new HashMap<>();

    @Override public List<CookBookEntity> getList(@WebParam(name = "entityIds", targetNamespace = "") List<Integer> entityIds)
            throws NoSuchEntityException, SessionExpiredException {
        List<CookBookEntity> returnValue = new ArrayList<>();
        for (Integer id : entityIds) {
            returnValue.add(get(id));
        }
        return returnValue;
    }

    @Override public void delete(@WebParam(name = "id", targetNamespace = "") int id) throws NoSuchEntityException, SessionExpiredException {
        if (!entities.containsKey(id)) {
            throw new NoSuchEntityException();
        }
        entities.remove(id);
    }

    @Override public CookBookEntity create(@WebParam(name = "entity", targetNamespace = "") CookBookEntity entity) throws SessionExpiredException, InvalidEntityException {
        if (entity.getId() != null) {
            InvalidEntity invalid = new InvalidEntity();
            throw new InvalidEntityException("Cannot specify Id at creation.", invalid);
        }
        if (entity instanceof Ingredient) {
            Ingredient ingredient = (Ingredient) entity;
        }
        if (entity instanceof Recipe) {
            Recipe recipe = (Recipe) entity;

        }
        entity.setId(++currentIndex);
        entities.put(entity.getId(), entity);
        return entity;
    }

    @Override public CookBookEntity update(@WebParam(name = "entity", targetNamespace = "") CookBookEntity entity)
            throws NoSuchEntityException, SessionExpiredException, InvalidEntityException {
        if (!entities.containsKey(entity.getId())) {
            throw new NoSuchEntityException();
        }
        entities.put(entity.getId(), entity);
        return entity;
    }

    @Override public CookBookEntity get(@WebParam(name = "id", targetNamespace = "") int id) throws NoSuchEntityException, SessionExpiredException {

        if (!entities.containsKey(id)) {

            throw new NoSuchEntityException();
        }
        return entities.get(id);
    }

    @Override public List<CookBookEntity> updateList(@WebParam(name = "entities", targetNamespace = "") List<CookBookEntity> entities)
            throws NoSuchEntityException, SessionExpiredException, InvalidEntityException {
        for (CookBookEntity entity : entities) {
            update(entity);
        }
        return entities;
    }

    @Override public Recipe updateQuantities(@WebParam(name = "arg0", targetNamespace = "") Recipe arg0)
            throws NoSuchEntityException, SessionExpiredException, InvalidEntityException {
        return null;
    }

    @Override public void deleteList(@WebParam(name = "entityIds", targetNamespace = "") List<Integer> entityIds) throws NoSuchEntityException, SessionExpiredException {
        for (Integer id : entityIds) {
            delete(id);
        }
    }

    @Override public List<CookBookEntity> addList(@WebParam(name = "entities", targetNamespace = "") List<CookBookEntity> entities)
            throws SessionExpiredException, InvalidEntityException {
        for (CookBookEntity entity : entities) {
            create(entity);
        }
        return entities;
    }

    @Override public List<CookBookEntity> searchWithQuery(@WebParam(name = "query", targetNamespace = "") String query, @WebParam(name = "page", targetNamespace = "") Integer page,
            @WebParam(name = "pageSize", targetNamespace = "") Integer pageSize) throws NoSuchEntityException, SessionExpiredException {
        try {
            DsqlParser parser = Parboiled.createParser(DsqlParser.class);
            ParsingResult<?> result = new ReportingParseRunner(parser.Statement()).run(query);
            if(result.hasErrors()){

                throw new NoSuchEntityException(result.parseErrors.get(0).getErrorMessage());
            }
            return CollectionUtils.arrayToList(entities.values().toArray());
        } catch (java.lang.Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    @Override public List<Recipe> getRecentlyAdded() {
        Collection<CookBookEntity> values = entities.values();
        List<Recipe> recipies = new ArrayList<Recipe>();
        for(CookBookEntity entity : values){
            if(entity instanceof Recipe){
                recipies.add((Recipe)entity);
            }
        }
        return CollectionUtils.arrayToList(recipies.toArray());
    }

    @Override public Description describeEntity(@WebParam(name = "entity", targetNamespace = "") CookBookEntity entity)
            throws NoSuchEntityException, SessionExpiredException, InvalidEntityException {
        Description description = null;
        if(entity.getId() == null || entity.getId()==0){
            if( entity instanceof Ingredient){
                description = getIngredientDescription();
            }else{
                description = getRecipeDescription(entity.getId());
            }
        } else {
            if( entity instanceof Ingredient){
                throw new InvalidEntityException("You cannot describe specific ingredients");
            }else{
                description = getRecipeDescription(entity.getId());
            }
        }
        return description;
    }

    private Description getRecipeDescription(Integer id) throws SessionExpiredException, NoSuchEntityException {
        Description description = new Description();
        List<Description> fields = new ArrayList<Description>();
        description.setName("Recipe");
        populateCookBookEntityFields(description, fields);
        addRecipeIngredientFields(fields,id);
        description.setInnerFields(fields);
        return description;
    }

    private void addRecipeIngredientFields(List<Description> fields, Integer id) throws SessionExpiredException, NoSuchEntityException {
        /*
        private List<Ingredient> ingredients;
        private Double prepTime;
        private Double cookTime;
        private List<String> directions;
        */
        Description field;
        /*
        private double quantity;
        private UnitType unit;
        */
        field = new Description();
        field.setName("ingredients");
        field.setDataType(DataType.LIST);
        field.setQuerable(true);
        field.setSortable(true);
        field.setInnerType("Ingredient");
        fields.add(field);
        if(id!=null && id!=0){
            loadRecipeFields(field,id);
        }

        field = new Description();
        field.setName("prepTime");
        field.setDataType(DataType.DOUBLE);
        field.setQuerable(true);
        field.setSortable(true);
        fields.add(field);

        field = new Description();
        field.setName("cookTime");
        field.setDataType(DataType.DOUBLE);
        field.setQuerable(true);
        field.setSortable(true);
        fields.add(field);

        field = new Description();
        field.setName("directions");
        field.setDataType(DataType.LIST);
        field.setQuerable(false);
        field.setSortable(false);
        field.setInnerType("String");
        fields.add(field);
    }

    private void loadRecipeFields(Description field, Integer id) throws SessionExpiredException, NoSuchEntityException {
        Recipe recipe= (Recipe) this.get(id);
        List<Description> innerFields = new ArrayList<Description>(recipe.getIngredients().size());
        for(Ingredient ing : recipe.getIngredients()){
            Description description = getIngredientDescription();
            description.setName(ing.getName());
            description.setQuerable(false);
            description.setSortable(false);
            innerFields.add(description);
        }
        field.setInnerFields(innerFields);
    }

    private Description getIngredientDescription() {
        Description description = new Description();
        List<Description> fields = new ArrayList<Description>();
        description.setName("Ingredient");
        populateCookBookEntityFields(description, fields);
        addSpecificIngredientFields(fields);
        description.setInnerFields(fields);
        return description;
    }

    private void addSpecificIngredientFields(List<Description> fields) {
        Description field;
        /*
        private double quantity;
        private UnitType unit;
        */
        field = new Description();
        field.setName("quantity");
        field.setDataType(DataType.DOUBLE);
        field.setQuerable(true);
        field.setSortable(true);
        fields.add(field);

        field = new Description();
        field.setName("unit");
        field.setDataType(DataType.UNIT_TYPE);
        field.setQuerable(true);
        field.setSortable(true);
        fields.add(field);
    }

    /**
     * Populate common CookBookEntity fields
     *
     * @param description The object representing the description of the CookBookEntity
     * @param fields The list of fields we have to populate.
     */
    private void populateCookBookEntityFields(Description description, List<Description> fields) {
        description.setDataType(DataType.OBJECT);
        description.setQuerable(true);
        description.setSortable(false);

        Description field = null;
        /*
        private Integer id;
        private Date created;
        private Date lastModified;
        private String name;
         */
        field = new Description();
        field.setName("id");
        field.setDataType(DataType.INTEGER);
        field.setQuerable(true);
        field.setSortable(true);
        fields.add(field);

        field = new Description();
        field.setName("created");
        field.setDataType(DataType.DATE);
        field.setQuerable(false);
        field.setSortable(false);
        fields.add(field);

        field = new Description();
        field.setName("lastModified");
        field.setDataType(DataType.DATE);
        field.setQuerable(false);
        field.setSortable(false);
        fields.add(field);

        field = new Description();
        field.setName("name");
        field.setDataType(DataType.STRING);
        field.setQuerable(true);
        field.setSortable(true);
        fields.add(field);
    }
}