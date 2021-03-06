/*
 *     This file is part of ToroDB.
 *
 *     ToroDB is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ToroDB is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with ToroDB. If not, see <http://www.gnu.org/licenses/>.
 *
 *     Copyright (c) 2014, 8Kdata Technology
 *     
 */

package com.torodb.torod.db.postgresql.query;

import com.torodb.torod.core.language.querycriteria.OrQueryCriteria;
import com.torodb.torod.core.language.querycriteria.ModIsQueryCriteria;
import com.torodb.torod.core.language.querycriteria.IsObjectQueryCriteria;
import com.torodb.torod.core.language.querycriteria.SizeIsQueryCriteria;
import com.torodb.torod.core.language.querycriteria.InQueryCriteria;
import com.torodb.torod.core.language.querycriteria.FalseQueryCriteria;
import com.torodb.torod.core.language.querycriteria.IsGreaterQueryCriteria;
import com.torodb.torod.core.language.querycriteria.NotQueryCriteria;
import com.torodb.torod.core.language.querycriteria.ExistsQueryCriteria;
import com.torodb.torod.core.language.querycriteria.IsGreaterOrEqualQueryCriteria;
import com.torodb.torod.core.language.querycriteria.ContainsAttributesQueryCriteria;
import com.torodb.torod.core.language.querycriteria.QueryCriteria;
import com.torodb.torod.core.language.querycriteria.IsLessQueryCriteria;
import com.torodb.torod.core.language.querycriteria.AndQueryCriteria;
import com.torodb.torod.core.language.querycriteria.IsLessOrEqualQueryCriteria;
import com.torodb.torod.core.language.querycriteria.TrueQueryCriteria;
import com.torodb.torod.core.language.querycriteria.IsEqualQueryCriteria;
import com.torodb.torod.core.language.querycriteria.TypeIsQueryCriteria;
import com.torodb.torod.db.postgresql.query.processors.TrueProcessor;
import com.torodb.torod.db.postgresql.query.processors.ModIsProcessor;
import com.torodb.torod.db.postgresql.query.processors.FalseProcessor;
import com.torodb.torod.db.postgresql.query.processors.IsEqualProcessor;
import com.torodb.torod.db.postgresql.query.processors.NotProcessor;
import com.torodb.torod.db.postgresql.query.processors.OrProcessor;
import com.torodb.torod.db.postgresql.query.processors.InProcessor;
import com.torodb.torod.db.postgresql.query.processors.SizeIsProcessor;
import com.torodb.torod.db.postgresql.query.processors.CompareProcessor;
import com.torodb.torod.db.postgresql.query.processors.TypeIsProcessor;
import com.torodb.torod.db.postgresql.query.processors.AndProcessor;
import com.torodb.torod.db.postgresql.query.processors.ExistsProcessor;
import com.torodb.torod.db.postgresql.query.processors.ContainsAttributesProcessor;
import com.torodb.torod.core.language.querycriteria.utils.QueryCriteriaVisitor;
import com.torodb.torod.db.postgresql.query.processors.IsObjectProcessor;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class Processor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Processor.class);
    
    public Result process(QueryCriteria query) {
        ProcessorVisitor visitor = new ProcessorVisitor();

        visitor.process(query);
        
        LOGGER.debug("Query {} translated to {}", query, visitor);
        
        return visitor;
    }

    public static interface Result {

        public ExistRelation getExistRelation();

        public List<ProcessedQueryCriteria> getProcessedQueryCriterias();
    }

    private static class ProcessorVisitor implements QueryCriteriaVisitor<List<ProcessedQueryCriteria>, Void>, Result {

        private List<ProcessedQueryCriteria> processedQueryCriterias;
        private final ExistRelation existRelation = new ExistRelation();

        public void process(QueryCriteria query) {
            processedQueryCriterias = query.accept(this, null);
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(
                    100 * processedQueryCriterias.size()
            );
            for (ProcessedQueryCriteria processedQueryCriteria : processedQueryCriterias) {
                sb.append("\n\t· ").append(processedQueryCriteria.toString());
            }
            return sb.toString();
        }
        
        @Override
        public ExistRelation getExistRelation() {
            return existRelation;
        }

        @Override
        public List<ProcessedQueryCriteria> getProcessedQueryCriterias() {
            return processedQueryCriterias;
        }

        @Override
        public List<ProcessedQueryCriteria> visit(TrueQueryCriteria criteria, Void arg) {
            return TrueProcessor.process(criteria, this);
        }

        @Override
        public List<ProcessedQueryCriteria> visit(FalseQueryCriteria criteria, Void arg) {
            return FalseProcessor.process(criteria, this);
        }

        @Override
        public List<ProcessedQueryCriteria> visit(AndQueryCriteria criteria, Void arg) {
            return AndProcessor.process(criteria, this);
        }

        @Override
        public List<ProcessedQueryCriteria> visit(OrQueryCriteria criteria, Void arg) {
            return OrProcessor.process(criteria, this);
        }

        @Override
        public List<ProcessedQueryCriteria> visit(NotQueryCriteria criteria, Void arg) {
            return NotProcessor.process(criteria, this);
        }

        @Override
        public List<ProcessedQueryCriteria> visit(TypeIsQueryCriteria criteria, Void arg) {
            return TypeIsProcessor.process(criteria, this);
        }

        @Override
        public List<ProcessedQueryCriteria> visit(IsEqualQueryCriteria criteria, Void arg) {
            return IsEqualProcessor.process(criteria, this);
        }

        @Override
        public List<ProcessedQueryCriteria> visit(IsGreaterQueryCriteria criteria, Void arg) {
            return CompareProcessor.process(criteria, this);
        }

        @Override
        public List<ProcessedQueryCriteria> visit(IsGreaterOrEqualQueryCriteria criteria, Void arg) {
            return CompareProcessor.process(criteria, this);
        }

        @Override
        public List<ProcessedQueryCriteria> visit(IsLessQueryCriteria criteria, Void arg) {
            return CompareProcessor.process(criteria, this);
        }

        @Override
        public List<ProcessedQueryCriteria> visit(IsLessOrEqualQueryCriteria criteria, Void arg) {
            return CompareProcessor.process(criteria, this);
        }

        @Override
        public List<ProcessedQueryCriteria> visit(InQueryCriteria criteria, Void arg) {
            return InProcessor.process(criteria, this);
        }

        @Override
        public List<ProcessedQueryCriteria> visit(IsObjectQueryCriteria criteria, Void arg) {
            return IsObjectProcessor.process(criteria, this);
        }

        @Override
        public List<ProcessedQueryCriteria> visit(ModIsQueryCriteria criteria, Void arg) {
            return ModIsProcessor.process(criteria, this);
        }

        @Override
        public List<ProcessedQueryCriteria> visit(SizeIsQueryCriteria criteria, Void arg) {
            return SizeIsProcessor.process(criteria, this);
        }

        @Override
        public List<ProcessedQueryCriteria> visit(ContainsAttributesQueryCriteria criteria, Void arg) {
            return ContainsAttributesProcessor.process(criteria, this);
        }

        @Override
        public List<ProcessedQueryCriteria> visit(ExistsQueryCriteria criteria, Void arg) {
            return ExistsProcessor.process(criteria, this, existRelation);
        }
    }

}
