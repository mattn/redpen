/**
 * redpen: a text inspection tool
 * Copyright (c) 2014-2015 Recruit Technologies Co., Ltd. and contributors
 * (see CONTRIBUTORS.md)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cc.redpen.parser;

import cc.redpen.RedPenException;
import cc.redpen.model.Document;
import cc.redpen.model.Section;
import cc.redpen.model.Sentence;
import cc.redpen.parser.latex.LaTeXProcessor;
import cc.redpen.tokenizer.RedPenTokenizer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Parser for LaTeX format (based on the Markdown parser.)
 */
class LaTeXParser extends BaseDocumentParser {

    LaTeXParser() {
        super();
    }

    /* XXX: huge duplication over the base */
    @Override
    public Document parse(InputStream inputStream, Optional<String> fileName, SentenceExtractor sentenceExtractor, RedPenTokenizer tokenizer)
            throws RedPenException {
        Document.DocumentBuilder documentBuilder = Document.builder(tokenizer);
        fileName.ifPresent(documentBuilder::setFileName);

        StringBuilder sb = new StringBuilder();
        String line;
        int charCount = 0;
        List<Integer> lineList = new ArrayList<>();
        PreprocessingReader br = createReader(inputStream);

        try {
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
                // TODO surrogate pair ?
                charCount += line.length() + 1;
                lineList.add(charCount);
            }
        } catch (IOException e) {
            throw new RedPenException(e);
        }

        List<Sentence> headers = new ArrayList<>();
        headers.add(new Sentence("", 0));
        documentBuilder.appendSection(new Section(0, headers));

        new LaTeXProcessor().parse(sb.toString().toCharArray(), documentBuilder, sentenceExtractor);

        documentBuilder.setPreprocessorRules(br.getPreprocessorRules());

        return documentBuilder.build();
    }
}
