/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.github.event

import reactor.core.publisher.Mono

/**
 * @author Rob Winch
 */
interface GithubEventService {
    // FIXME: Return a status with description of why not backported and return that in the response body of controller
    /**
     * Handle a PushEvent. Closes a backported issue when Fixes: gh-<original-ticket-id>
     * was pushed to the corresponding branch.
     */
    fun backport(pushEvent : PushEvent) : Mono<Boolean>

    /**
     * Handle a PushEvent. Closes a backported issue when Fixes: gh-<original-ticket-id>
     * was pushed to the corresponding branch.
     */
    fun backport(pullRequestEvent: PullRequestEvent): Mono<Boolean>

    /**
     * Looks for adding a label of "backport: 1.2.x" and then creates a Backport issue
     * if not already created. It then removes the label.
     */
    fun backport(issueEvent: IssueEvent) : Mono<Boolean>
}