package org.molgenis.data.annotation.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AbstractRepositoryAnnotator;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.annotation.impl.datastructures.CosmicData;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * <p>
 * This class calls the Ensembl webservice with a ensemble ID. The webservice returns a map with information on the
 * somatic variants associated with the gene
 * </p>
 * 
 * @author bcharbon
 * 
 * */
@Component("cosmicService")
public class CosmicServiceAnnotator extends AbstractRepositoryAnnotator implements RepositoryAnnotator,
		ApplicationListener<ContextRefreshedEvent>
{
	// Web url to call the ensembl web service
	private static final String SERVICE_URL = "http://beta.rest.ensembl.org/feature/id/";
	private static final String SERVICE_POSTFIX = ".json?feature=somatic_variation";
	// ensembl service is dependant on this ID when the web service is called
	// If ensemblId is not in a data set, the web service cannot be used
	public static final String ENSEMBLE_ID = "ensemblId";
	public static final String NAME = "Cosmic";
	public static final String ID = "ID";
	public static final String FEATURE_TYPE = "feature_type";
	public static final String ALT_ALLELES = "alt_alleles";
	public static final String END = "end";
	public static final String SEQ_REGION_NAME = "seq_region_name";
	public static final String CONSEQUENCE_TYPE = "consequence_type";
	public static final String STRAND = "strand";
	public static final String START = "start";
	private final DefaultHttpClient httpClient;

	public CosmicServiceAnnotator()
	{
		httpClient = new DefaultHttpClient();
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public EntityMetaData getInputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(ENSEMBLE_ID, FieldTypeEnum.STRING));
		return metadata;
	}

	@Override
	public boolean annotationDataExists()
	{
		boolean dataExists = true;

		// TODO check if the webservice is up and running

		return dataExists;
	}

	@Override
	public List<Entity> annotateEntity(Entity entity)
	{
		HttpGet httpGet = new HttpGet(getServiceUri(entity));
		ArrayList<Entity> resultEntities = new ArrayList<Entity>();
		List<Object> annotatedInput = new ArrayList<Object>();
		if (!annotatedInput.contains(entity.get(ENSEMBLE_ID)))
		{
			annotatedInput.add(entity.get(ENSEMBLE_ID));
			try
			{
				HttpResponse response = httpClient.execute(httpGet);
				BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent()),
						Charset.forName("UTF-8")));

				String output;
				StringBuilder result = new StringBuilder();

				while ((output = br.readLine()) != null)
				{
					result.append(output);
				}
				resultEntities.addAll(parseResult(entity, result.toString()));
			}
			catch (Exception e)
			{
				httpGet.abort();
				// TODO: how to handle exceptions at this point
				throw new RuntimeException(e);
			}
		}
		return resultEntities;
	}

	private String getServiceUri(Entity entity)
	{
		StringBuilder uriStringBuilder = new StringBuilder();
		uriStringBuilder.append(SERVICE_URL);
		uriStringBuilder.append(entity.get(ENSEMBLE_ID));
		uriStringBuilder.append(SERVICE_POSTFIX);

		return uriStringBuilder.toString();
	}

	private List<Entity> parseResult(Entity entity, String json) throws IOException
	{
		List<Entity> result = new ArrayList<Entity>();
		if (!"".equals(json))
		{
			Collection<CosmicData> resultCollection = jsonStringToCollection(json);
			for (CosmicData data : resultCollection)
			{
				Entity resultEntity = new MapEntity();
				resultEntity.set(ID, data.getID());
				resultEntity.set(FEATURE_TYPE, data.getFeature_type());
				resultEntity.set(ALT_ALLELES, data.getAlt_alleles()[0] + "," + data.getAlt_alleles()[1]);
				resultEntity.set(END, data.getEnd());
				resultEntity.set(SEQ_REGION_NAME, data.getSeq_region_name());
				resultEntity.set(CONSEQUENCE_TYPE, data.getConsequence_type());
				resultEntity.set(STRAND, data.getStrand());
				resultEntity.set(START, data.getStart());
				resultEntity.set(ENSEMBLE_ID, entity.get(ENSEMBLE_ID));
				result.add(resultEntity);
			}
		}
		return result;
	}

	private static Collection<CosmicData> jsonStringToCollection(String result) throws IOException
	{
		Gson gson = new Gson();
		Type collectionType = new TypeToken<Collection<CosmicData>>()
		{
		}.getType();
		Collection<CosmicData> results = gson.fromJson(result, collectionType);

		return results;
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(ID, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(FEATURE_TYPE, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(ALT_ALLELES, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(END, FieldTypeEnum.INT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(SEQ_REGION_NAME, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(CONSEQUENCE_TYPE, FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(STRAND, FieldTypeEnum.INT));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(START, FieldTypeEnum.INT));
		return metadata;
	}
}
