package com.aleksgolds.common;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class FileList {
    public  static List<String> fileNameList (String path) throws IOException {
        return Files.list(Paths.get(path))
                .filter(o->!Files.isDirectory(o))
                .map(o->o.getFileName().toString())
                .collect(Collectors.toList());
    }
}
