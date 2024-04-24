/**
 * Copyright 2016 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.netflix.search.query.input;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.netflix.search.query.Properties;
import com.netflix.search.query.utils.StringUtils;

public class TitleWithQueries {
    public static final Logger logger = LoggerFactory.getLogger(TitleWithQueries.class);

	public static final String Q_ = "q_";
	public static final String TITLE_ALT = "title_alt";
	public static final String TITLE_LOCALE = "title_locale";
	public static final String TITLE_EN = "title_en";
	public static final String ID = "id";
	private static final String SHEET_NAME_DELIMITER = "-";
	private static final Joiner JOINER_QUERIES = Joiner.on("~~~");
    private static final Joiner JOINER_CATEGORIES = Joiner.on("=");

    private String id;
    private String titleEn;
    private String titleLocale;
    private String titleAlt;
    private final String language;
    private final String entityType;
    private final String sheetId;

    private final Map<String, Set<String>> queriesByCategory = Maps.newLinkedHashMap();

    public TitleWithQueries(String sheetId) {
        String[] id = sheetId.split(SHEET_NAME_DELIMITER);
        this.language = id[0];
        this.entityType = id[1];
        this.sheetId = sheetId;
    }

    public String getId()
    {
		return  StringUtils.createIdUsingTestName(id, sheetId);
    }

    public String getTitleEn()
    {
        return titleEn;
    }

    public String getTitleLocale()
    {
        return titleLocale;
    }

    public String getTitleAlt()
    {
        return titleAlt;
    }

    public String getLanguage()
    {
        return language;
    }

    public String getEntityType()
    {
        return entityType;
    }

    public Map<String, Set<String>> getQueriesByCategory()
    {
        return queriesByCategory;
    }

    public void setValue(String headerValue, String value)
    {
        if(headerValue==null){
            logger.error("Header is missing for this value: " + value);
            return;
        }
        if (value != null && !value.isEmpty()) {
            if (headerValue.equalsIgnoreCase(ID)) {
                this.id = value;
            }

            else if (headerValue.equalsIgnoreCase(TITLE_EN)) {
                this.titleEn = value;
            }

            else if (headerValue.equalsIgnoreCase(TITLE_LOCALE)) {
                this.titleLocale = value;
            }

            else if (headerValue.equalsIgnoreCase(TITLE_ALT)) {
                this.titleAlt = value;
            }

            else if (headerValue.startsWith(Q_)) {
                String cleanedHeader = headerValue.substring(2);
                Set<String> queriesForThisCategory = queriesByCategory.get(cleanedHeader);
                if (queriesForThisCategory == null) {
                    queriesForThisCategory = Sets.newLinkedHashSet();
                }
                queriesForThisCategory.add(value);
                queriesByCategory.put(cleanedHeader, queriesForThisCategory);
            }
        }
    }

    @Override
    public String toString()
    {
        List<String> mapToList = mapToList(queriesByCategory);
        return getId() + Properties.inputDelimiter.get() + titleEn + Properties.inputDelimiter.get() + titleLocale + Properties.inputDelimiter.get() + (titleAlt==null?"":titleAlt) + Properties.inputDelimiter.get()+"q=" + mapToList;
    }

    private List<String> mapToList(final Map<String, Set<String>> input)
    {
        return Lists.newArrayList(Iterables.transform(input.entrySet(), new Function<Map.Entry<String, Set<String>>, String>() {
            public String apply(final Map.Entry<String, Set<String>> input)
            {
                return JOINER_CATEGORIES.join(input.getKey(), JOINER_QUERIES.join(input.getValue()));
            }
        }));
    }

}
