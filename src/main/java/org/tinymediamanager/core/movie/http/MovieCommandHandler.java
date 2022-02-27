/*
 * Copyright 2012 - 2020 Manuel Laggner
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
package org.tinymediamanager.core.movie.http;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinymediamanager.core.http.AbstractCommandHandler;
import org.tinymediamanager.core.http.InvalidCommandException;
import org.tinymediamanager.core.http.TmmCommandResponse;
import org.tinymediamanager.core.threading.TmmTaskManager;

public class MovieCommandHandler extends AbstractCommandHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(MovieCommandHandler.class);

  @Override
  protected TmmCommandResponse processCommands(List<Command> commands) throws Exception {
    checkCommands(commands);
    TmmTaskManager.getInstance().addMainTask(new MovieCommandTask(commands));

    return new TmmCommandResponse(200, "commands prepared");
  }

  private void checkCommands(List<Command> commands) throws InvalidCommandException {
    // 1. check update params

  }
}
