package com.contentiq.contentiq.service;

import com.contentiq.contentiq.factory.ContentProcessorFactory;
import com.contentiq.contentiq.model.NotesDocument;
import com.contentiq.contentiq.model.Video;
import com.contentiq.contentiq.observer.AnalysisEventPublisher;
import com.contentiq.contentiq.repository.AnalysisReportRepository;
import com.contentiq.contentiq.repository.NotesDocumentRepository;
import org.springframework.stereotype.Service;

@Service
public class AINotesGeneratorService extends NotesGeneratorService {

    public AINotesGeneratorService(VideoService videoService,
                                   ContentProcessorFactory factory,
                                   NotesDocumentRepository notesRepository,
                                   AnalysisReportRepository reportRepository,
                                   AnalysisEventPublisher eventPublisher) {
        super(videoService, factory, notesRepository, reportRepository, eventPublisher);
    }

    @Override
    protected NotesDocument produce(Video video) {
        return factory.notes().analyze(video);
    }
}
