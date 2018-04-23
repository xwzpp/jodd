// Copyright (c) 2003-present, Jodd Team (http://jodd.org)
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// 1. Redistributions of source code must retain the above copyright notice,
// this list of conditions and the following disclaimer.
//
// 2. Redistributions in binary form must reproduce the above copyright
// notice, this list of conditions and the following disclaimer in the
// documentation and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.

package jodd.madvoc.result;

import jodd.io.StreamUtil;
import jodd.json.JsonSerializer;
import jodd.madvoc.ActionRequest;
import jodd.madvoc.MadvocConfig;
import jodd.madvoc.meta.In;
import jodd.madvoc.meta.Scope;
import jodd.madvoc.scope.MadvocScope;
import jodd.util.net.MimeTypes;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;

/**
 * Json results handler.
 */
public class JsonActionResult implements ActionResult {

	@In @Scope(MadvocScope.CONTEXT)
	protected MadvocConfig madvocConfig;

	@Override
	public void render(final ActionRequest actionRequest, final Object object) throws Exception {
		HttpServletResponse response = actionRequest.getHttpServletResponse();

		String encoding = response.getCharacterEncoding();

		if (encoding == null) {
			encoding = madvocConfig.getEncoding();
		}

		response.setContentType(MimeTypes.MIME_APPLICATION_JSON);
		response.setCharacterEncoding(encoding);

		final String json;
		final int status;
		final String statusMessage;

		if (object instanceof JsonResult) {
			JsonResult jsonResult = (JsonResult) object;

			json = jsonResult.value();
			status = jsonResult.status();
			statusMessage = jsonResult.message();
		}
		else {
			json = JsonSerializer.create().deep(true).serialize(object);
			status = 200;
			statusMessage = "OK";
		}

		byte[] data = json.getBytes(encoding);
		response.setContentLength(data.length);

		OutputStream out = null;
		try {
			out = response.getOutputStream();
			out.write(data);

			if (status < 400) {
				response.setStatus(status);
			}
			else {
				response.sendError(status, statusMessage);
			}

		} finally {
			StreamUtil.close(out);
		}
	}
}
