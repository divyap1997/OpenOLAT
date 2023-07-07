/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.cemedia.manager;

import static org.olat.core.commons.persistence.PersistenceHelper.appendFuzzyLike;
import static org.olat.core.commons.persistence.PersistenceHelper.makeFuzzyQueryString;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.TypedQuery;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.id.Identity;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.ceditor.manager.ContentEditorFileStorage;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaLight;
import org.olat.modules.cemedia.MediaToGroupRelation.MediaToGroupRelationType;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.cemedia.model.MediaIdentityNames;
import org.olat.modules.cemedia.model.MediaImpl;
import org.olat.modules.cemedia.model.MediaUsage;
import org.olat.modules.cemedia.model.MediaUsageWithStatus;
import org.olat.modules.cemedia.model.MediaVersionImpl;
import org.olat.modules.cemedia.model.MediaWithVersion;
import org.olat.modules.cemedia.model.SearchMediaParameters;
import org.olat.modules.cemedia.model.SearchMediaParameters.Scope;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 17.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class MediaDAO {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private ContentEditorFileStorage fileStorage;
	
	/**
	 * 
	 * @param title
	 * @param description
	 * @param content
	 * @param type
	 * @param businessPath
	 * @param referenceId Some external id
	 * @param signature
	 * @param author
	 * @return
	 */
	public Media createMedia(String title, String description, String altText, String type, String businessPath,
			String referenceId, int signature, Identity author) {
		MediaImpl media = new MediaImpl();
		media.setCreationDate(new Date());
		media.setCollectionDate(media.getCreationDate());
		media.setVersions(new ArrayList<>());
		media.setType(type);
		media.setReferenceId(referenceId);
		media.setUuid(UUID.randomUUID().toString());
		
		media.setTitle(title);
		media.setDescription(description);
		media.setAltText(altText);
		media.setSignature(signature);
		media.setBusinessPath(businessPath);
		
		media.setAuthor(author);
		dbInstance.getCurrentEntityManager().persist(media);
		return media;
	}
	
	public Media createMedia(String title, String description, String altText, String content, String type, String businessPath,
			String referenceId, int signature, Identity author) {
		Media media = createMedia(title, description, altText, type, businessPath, referenceId, signature, author);
		return createVersion(media, new Date(), content, null, null);
	}
	
	public Media createVersion(Media media, Date collectionDate, String content, String storage, String rootFilename) {
		MediaVersionImpl version = new MediaVersionImpl();
		version.setCreationDate(new Date());
		version.setCollectionDate(collectionDate);
		version.setVersionName(Integer.toString(media.getVersions().size()));
		version.setVersionUuid(UUID.randomUUID().toString());
		version.setContent(content);
		version.setStoragePath(storage);
		version.setRootFilename(rootFilename);
		checksumAndMetadata(version);
		version.setMedia(media);
		dbInstance.getCurrentEntityManager().persist(version);
		media.getVersions().add(version);
		return dbInstance.getCurrentEntityManager().merge(media);
	}
	
	public Media setVersion(Media media, Date collectionDate) {
		List<MediaVersion> versions = media.getVersions();
		if(versions == null || versions.isEmpty()) {
			return media;
		}
		MediaVersion currentVersion = media.getVersions().get(0);
		return addVersion(media, collectionDate, currentVersion.getContent(),
				currentVersion.getStoragePath(), currentVersion.getRootFilename());
	}
	
	public Media restoreVersion(Media media, Date collectionDate, MediaVersion mediaVersion) {
		List<MediaVersion> versions = media.getVersions();
		if(versions == null || versions.isEmpty()) {
			return media;
		}
		return addVersion(media, collectionDate, mediaVersion.getContent(),
				mediaVersion.getStoragePath(), mediaVersion.getRootFilename());
	}
	
	public Media addVersion(Media media, Date collectionDate, String content, String storage, String rootFilename) {
		List<MediaVersion> versions = media.getVersions();
		if(versions == null || versions.isEmpty()) {
			return createVersion(media, collectionDate, content, storage, rootFilename);
		}
		
		MediaVersionImpl currentVersion = (MediaVersionImpl)media.getVersions().get(0);
		
		MediaVersionImpl newVersion = new MediaVersionImpl();
		newVersion.setCreationDate(currentVersion.getCreationDate());
		newVersion.setCollectionDate(currentVersion.getCollectionDate());
		newVersion.setVersionName(Integer.toString(media.getVersions().size()));
		newVersion.setVersionUuid(currentVersion.getVersionUuid());
		newVersion.setVersionChecksum(currentVersion.getVersionChecksum());
		newVersion.setContent(currentVersion.getContent());
		newVersion.setStoragePath(currentVersion.getStoragePath());
		newVersion.setRootFilename(currentVersion.getRootFilename());
		newVersion.setMetadata(currentVersion.getMetadata());
		newVersion.setMedia(media);
		
		currentVersion.setCollectionDate(collectionDate);
		currentVersion.setVersionUuid(UUID.randomUUID().toString());
		currentVersion.setContent(content);
		currentVersion.setStoragePath(storage);
		currentVersion.setRootFilename(rootFilename);
		checksumAndMetadata(currentVersion);
		dbInstance.getCurrentEntityManager().persist(newVersion);
		dbInstance.getCurrentEntityManager().merge(currentVersion);
		media.getVersions().add(1, newVersion);
		return dbInstance.getCurrentEntityManager().merge(media);
	}
	
	public void checksumAndMetadata(MediaVersionImpl version) {
		if(StringHelper.containsNonWhitespace(version.getRootFilename())) {
			File rootFile = fileStorage.getMediaRootFile(version);
			if(rootFile != null && rootFile.exists()) {
				version.setVersionChecksum(FileUtils.checksumSha256(rootFile));
			}
			
			VFSMetadata metadata = fileStorage.getMediaRootItemMetadata(version);
			if(metadata != null) {
				version.setMetadata(metadata);
			}
		}
	}
	
	public List<MediaVersion> getVersions(Media media) {
		StringBuilder sb = new StringBuilder();
		sb.append("select mversion from mediaversion as mversion")
		  .append(" where mversion.media.key=:mediaKey")
		  .append(" order by mversion.pos asc");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), MediaVersion.class)
				.setParameter("mediaKey", media.getKey())
				.getResultList();
	}
	
	public Media update(Media media) {
		return dbInstance.getCurrentEntityManager().merge(media);
	}
	
	public MediaVersion update(MediaVersion mediaVersion) {
		return dbInstance.getCurrentEntityManager().merge(mediaVersion);
	}
	
	public Media loadByKey(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select media from mmedia as media")
		  .append(" inner join fetch media.author as author")
		  .append(" where media.key=:mediaKey");
		
		List<Media> medias = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Media.class)
				.setParameter("mediaKey", key)
				.getResultList();
		return medias == null || medias.isEmpty() ? null : medias.get(0);
	}
	
	public Media loadByUuid(String uuid) {
		StringBuilder sb = new StringBuilder();
		sb.append("select media from mmedia as media")
		  .append(" inner join fetch media.author as author")
		  .append(" where media.uuid=:uuid");
		
		List<Media> medias = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Media.class)
				.setParameter("uuid", uuid)
				.getResultList();
		return medias == null || medias.isEmpty() ? null : medias.get(0);
	}
	
	/**
	 * 
	 * @param identity The identity
	 * @param mediaKeys A list of medias primary keys
	 * @return A sub list of the specified media keys which are owned by the
	 *         specified identity and not used in a media part.
	 */
	public List<Long> filterOwnedDeletableMedias(IdentityRef identity, List<Long> mediaKeys) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select media.key")
		  .append(" from mmedia as media")
		  .append(" inner join media.author as author")
		  .append(" where author.key=:identityKey");
		boolean useInForKeys = mediaKeys.size() <= 255;
		if(useInForKeys) {
			sb.append(" and media.key in (:mediaKeys)");
		}
		sb.append(" and not exists(select mediaPart from cemediapart mediaPart")
		  .append("  where mediaPart.media.key=media.key")
		  .append(" )");
		
		TypedQuery<Long> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("identityKey", identity.getKey());
		if(useInForKeys) {
			query.setParameter("mediaKeys", mediaKeys);
		}
		List<Long> ownedKeys = query.getResultList();
		if(!useInForKeys) {
			ownedKeys.retainAll(mediaKeys);
		}
		return ownedKeys;
	}
	
	public List<MediaWithVersion> searchBy(SearchMediaParameters parameters) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select media, mversion, metadata,")
		  .append(" (select count(cVersion.key) from mediaversion as cVersion")
		  .append("  where cVersion.media.key=media.key")
		  .append(" ) as numOfVersions")
		  .append(" from mmedia as media")
		  .append(" left join fetch media.author as author")
		  .append(" left join fetch mediaversion as mversion on (mversion.media.key=media.key and mversion.pos=0)")
		  .append(" left join fetch mversion.metadata as metadata");
		
		Long identityKey = null;
		Long repositoryEntryKey = null;
		
		 if(parameters.getScope() == Scope.SHARED_WITH_ENTRY) {
			repositoryEntryKey = parameters.getRepositoryEntry().getKey();
			sb.and().append(" exists (select baseEntryRel.key from mediatogroup as baseEntryRel")
			  .append("  where baseEntryRel.media.key=media.key and baseEntryRel.repositoryEntry.key=:entryKey")
			  .append(")");
		} else if(parameters.getIdentity() != null) {
			if(parameters.getScope() == Scope.MY || parameters.getScope() == null) {
				identityKey = parameters.getIdentity().getKey();
				sb.and().append("author.key=:identityKey");
			} else {
				identityKey = parameters.getIdentity().getKey();
				
				sb.and().append("(");
				if(parameters.getScope() == Scope.SHARED_WITH_ME || parameters.getScope() == Scope.ALL) {
					if(parameters.getScope() == Scope.SHARED_WITH_ME) {
						sb.append("author.key<>:identityKey and");
					} else {
						sb.append("author.key=:identityKey or");
					}
					sb.append(" (exists (select shareRel.key from mediatogroup as shareRel")
					  .append("  inner join shareRel.group as uGroup")
					  .append("  inner join uGroup.members as uMember")
					  .append("  where shareRel.media.key=media.key and uMember.identity.key=:identityKey")
					  .append("  and (shareRel.type").in(MediaToGroupRelationType.USER)
					  .append("   or shareRel.type").in(MediaToGroupRelationType.BUSINESS_GROUP)
					  .append("   or (shareRel.type").in(MediaToGroupRelationType.ORGANISATION).append(" and uMember.role").in(OrganisationRoles.author, OrganisationRoles.learnresourcemanager, OrganisationRoles.administrator).append(")")
					  .append("))");
					
					sb.append(" or exists (select shareReRel.key from mediatogroup as shareReRel")
					  .append("  inner join shareReRel.repositoryEntry as v")
					  .append("  inner join v.groups as relGroup")
					  .append("  inner join relGroup.group as vBaseGroup")
			          .append("  inner join vBaseGroup.members as vMembership")
					  .append("  where shareReRel.media.key=media.key and shareReRel.type").in(MediaToGroupRelationType.REPOSITORY_ENTRY)
					  .append("    and vMembership.identity.key=:identityKey and vMembership.role").in(GroupRoles.owner, OrganisationRoles.learnresourcemanager, OrganisationRoles.administrator)
					  .append(")");

					sb.append(")");
				} else if(parameters.getScope() == Scope.SHARED_BY_ME) {
					sb.append("author.key=:identityKey and");
					sb.append(" exists (select shareRel.key from mediatogroup as shareRel")
					  .append("  inner join shareRel.group as uGroup")
					  .append("  where shareRel.media.key=media.key")
					  .append(")");
				}
				sb.append(")");
			}
		}

		String searchString = parameters.getSearchString();
		if(StringHelper.containsNonWhitespace(searchString)) {
			searchString = makeFuzzyQueryString(searchString);
			sb.and().append("(");
			appendFuzzyLike(sb, "media.title", "searchString", dbInstance.getDbVendor());
			sb.append(" or ");
			appendFuzzyLike(sb, "media.description", "searchString", dbInstance.getDbVendor());
			sb.append(")");
		}
		
		List<Long> tagKeys = parameters.getTags();
		if(tagKeys != null && !tagKeys.isEmpty()) {
			sb.and()
			  .append(" exists (select rel.key from mediatag as rel")
			  .append("  where rel.tag.key in (:tagKeys) and rel.media.key=media.key")
			  .append(" )");
		}
		
		List<String> types = parameters.getTypes();
		if(types != null && !types.isEmpty()) {
			sb.and().append("media.type in (:types)");
		}
		
		String checksum = parameters.getChecksum();
		if(StringHelper.containsNonWhitespace(checksum)) {
			sb.and().append("mversion.versionChecksum=:checksum");
		}
		
		List<TaxonomyLevelRef> taxonomyLevels = parameters.getTaxonomyLevelsRefs();
		if(taxonomyLevels != null && !taxonomyLevels.isEmpty()) {
			sb.and()
			  .append("exists (select taxRel.key from mediatotaxonomylevel as taxRel")
			  .append("  inner join taxRel.taxonomyLevel as level")
			  .append("  where level.key in (:levelKeys) and taxRel.media.key=media.key")
			  .append(" )");
		}
		
		TypedQuery<Object[]> query = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class);
		if(identityKey != null) {
			query.setParameter("identityKey", identityKey);
		}
		if(StringHelper.containsNonWhitespace(searchString)) {
			query.setParameter("searchString", searchString.toLowerCase());
		}
		if(tagKeys != null && !tagKeys.isEmpty()) {
			query.setParameter("tagKeys", tagKeys);
		}
		if(types != null && !types.isEmpty()) {
			query.setParameter("types", types);
		}
		if(StringHelper.containsNonWhitespace(checksum)) {
			query.setParameter("checksum", checksum);
		}
		if(taxonomyLevels != null && !taxonomyLevels.isEmpty()) {
			List<Long> levelKeys = taxonomyLevels.stream()
					.map(TaxonomyLevelRef::getKey).toList();
			query.setParameter("levelKeys", levelKeys);
		}
		if(repositoryEntryKey != null) {
			query.setParameter("entryKey", repositoryEntryKey);
		}
		
		List<Object[]> objects = query.getResultList();
		List<MediaWithVersion> medias = new ArrayList<>(objects.size());
		for(Object[] object:objects) {
			Media media = (Media)object[0];
			MediaVersion mediaVersion = (MediaVersion)object[1];
			VFSMetadata metadata = (VFSMetadata)object[2];
			long numOfVersions = PersistenceHelper.extractPrimitiveLong(object, 3);
			medias.add(new MediaWithVersion(media, mediaVersion, metadata, numOfVersions));
		}
		return medias;
	}
	
	public List<Media> load(IdentityRef author) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select media from mmedia as media")
		  .append(" where media.author.key=:authorKey");
			
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Media.class)
				.setParameter("authorKey", author.getKey())
				.getResultList();
	}
	
	public boolean isUsed(MediaLight media) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select part.key from cepagepart as part")
		  .append(" where part.media.key=:mediaKey");
		
		List<Long> pageKey = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Long.class)
				.setParameter("mediaKey", media.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return pageKey != null && !pageKey.isEmpty() && pageKey.get(0) != null;
	}
	
	public boolean isEditable(IdentityRef identity, MediaLight media) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select media.key from mmedia as media")
		  .append(" left join media.author as author")
		  .append(" left join mediatogroup as mGroup on (mGroup.media.key=media.key and mGroup.editable=true)")
		  .append(" left join mGroup.group as baseGroup")
		  .append(" left join baseGroup.members as members")
		  .append(" where media.key=:mediaKey and (author.key=:identityKey or members.identity.key=:identityKey)");
		
		List<Long> editableMediaKeys = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), Long.class)
			.setParameter("mediaKey", media.getKey())
			.setParameter("identityKey", identity.getKey()).setFirstResult(0).setMaxResults(1)
			.getResultList();
		return editableMediaKeys != null && !editableMediaKeys.isEmpty()
				&& editableMediaKeys.get(0) != null && editableMediaKeys.get(0).longValue() > 0;
	}
	
	public List<MediaUsageWithStatus> getPortfolioUsages(IdentityRef identity, MediaLight media) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select page.key, page.title, page.status, ")
		  .append("  binder.key, binder.title,")
		  .append("  media.key, mediaVersion.key, mediaVersion.versionName,")
		  .append("  identUser.firstName, identUser.lastName,")
		  .append("  (select count(pageMember.key) from bgroupmember as pageMember")
		  .append("   inner join bgroupmember as mediaMember on (pageMember.identity.key=mediaMember.identity.key)")
		  .append("   inner join bgroup as mGroup on (mediaMember.group.key=mGroup.key)")
		  .append("   inner join mediatogroup as groupToMedia on (groupToMedia.group.key=mGroup.key)")
		  .append("   where pageMember.group.key=page.baseGroup.key and groupToMedia.media.key=media.key")
		  .append("  ) as numOfLinkedGroups,")
		  .append("  (select count(pageMember.key) from bgroupmember as pageMember")
		  .append("   where pageMember.group.key=page.baseGroup.key and pageMember.identity.key=:identityKey")
		  .append("  ) as numOfPageOwners")
		  .append(" from cepage as page")
		  .append(" inner join page.body as pageBody")
		  .append(" inner join treat(pageBody.parts as cemediapart) mediaPart")
		  .append(" inner join mediaPart.media as media")
		  .append(" left join mediaPart.mediaVersion as mediaVersion")
		  .append(" left join mediaPart.identity as ident")
		  .append(" left join ident.user as identUser")
		  .append(" left join page.section as section")
		  .append(" left join section.binder as binder")
		  .append(" where media.key=:mediaKey and not exists (")
		  .append("   select pageRef from cepagereference pageRef where pageRef.page.key=page.key")
		  .append(" )");
		
		List<Object[]> objects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("mediaKey", media.getKey())
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		List<MediaUsageWithStatus> usage = new ArrayList<>(objects.size());
		for(Object[] object:objects) {
			Long pageKey = (Long)object[0];
			String pageTitle = (String)object[1];
			String pageStatus = (String)object[2];
			
			
			Long binderKey = (Long)object[3];
			String binderTitle = (String)object[4];

			Long mediaKey = (Long)object[5];
			Long mediaVersionKey = (Long)object[6];
			String mediaVersionName = (String)object[7];
			
			String userFullName = toFullName((String)object[8], (String)object[9]);

			Long numOfLinkedUsers = PersistenceHelper.extractLong(object, 10);
			boolean linkedByUser = numOfLinkedUsers != null && numOfLinkedUsers.longValue() > 0l;
			Long numOfOwnerships = PersistenceHelper.extractLong(object, 11);
			boolean linkedByOwnership = numOfOwnerships != null && numOfOwnerships.longValue() > 0l;

			usage.add(new MediaUsageWithStatus(pageKey, pageTitle, pageStatus, binderKey, binderTitle,
					null, null, null, mediaKey, mediaVersionKey, mediaVersionName, userFullName,
					linkedByUser, linkedByOwnership));
		}
		return usage;
	}
	
	public List<MediaUsageWithStatus> getPageUsages(IdentityRef identity, MediaLight media) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select page.key, page.title, page.status, ")
		  .append("  v.key, ref.subIdent, v.displayname,")
		  .append("  media.key, mediaVersion.key, mediaVersion.versionName,")
		  .append("  identUser.firstName, identUser.lastName,")
		  .append("  (select count(mToGroup.key) from mediatogroup as mToGroup")
		  .append("   inner join mToGroup.group as baseGroup")
		  .append("   inner join repoentrytogroup as reToGroup on (baseGroup.key=reToGroup.group.key)")
		  .append("   where  mToGroup.media.key=media.key and reToGroup.entry.key=v.key")
		  .append("  ) as numOfLinkedGroups,")
		  .append("  (select count(pageRe.key) from repositoryentry as pageRe")
		  .append("   inner join pageRe.groups as pageRelGroup")
          .append("   inner join pageRelGroup.group as pageBaseGroup")
          .append("   inner join pageBaseGroup.members as pageMembership")
		  .append("   where pageRe.key=v.key and pageMembership.identity.key=:identityKey and pageMembership.role").in(GroupRoles.owner.name())
		  .append("  ) as numOfOwners")
		  .append(" from cepage as page")
		  .append(" inner join page.body as pageBody")
		  .append(" inner join treat(pageBody.parts as cemediapart) mediaPart")
		  .append(" inner join mediaPart.media as media")
		  .append(" left join mediaPart.mediaVersion as mediaVersion")
		  .append(" left join mediaPart.identity as ident")
		  .append(" left join ident.user as identUser")
		  .append(" inner join cepagereference ref on (ref.page.key=page.key)")
		  .append(" inner join ref.repositoryEntry as v")
		  .append(" where media.key=:mediaKey");
		
		List<Object[]> objects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("mediaKey", media.getKey())
				.setParameter("identityKey", identity.getKey())
				.getResultList();
		List<MediaUsageWithStatus> usage = new ArrayList<>(objects.size());
		for(Object[] object:objects) {
			Long pageKey = (Long)object[0];
			String pageTitle = (String)object[1];
			String pageStatus = (String)object[2];

			Long repoKey = (Long)object[3];
			String subIdent = (String)object[4];
			String repoDisplayname = (String)object[5];

			Long mediaKey = (Long)object[6];
			Long mediaVersionKey = (Long)object[7];
			String mediaVersionName = (String)object[8];
			
			String userFullName = toFullName((String)object[9], (String)object[10]);

			Long numOfLinkedGroups = PersistenceHelper.extractLong(object, 11);
			boolean linkedByGroup = numOfLinkedGroups != null && numOfLinkedGroups.longValue() > 0l;
			Long numOfOwners = PersistenceHelper.extractLong(object, 12);
			boolean linkedByOwnership = numOfOwners != null && numOfOwners.longValue() > 0l;

			usage.add(new MediaUsageWithStatus(pageKey, pageTitle, pageStatus, null, null,
					repoKey, subIdent, repoDisplayname, mediaKey, mediaVersionKey, mediaVersionName,
					userFullName, linkedByGroup, linkedByOwnership));
		}
		return usage;
	}
	
	private String toFullName(String firstName, String lastName) {
		if(StringHelper.containsNonWhitespace(firstName) && StringHelper.containsNonWhitespace(lastName)) {
			MediaIdentityNames names = new MediaIdentityNames(firstName, lastName);
			return userManager.getUserDisplayName(names);
		}
		return null;
	}
	
	public List<MediaUsage> getUsages(MediaLight media) {
		QueryBuilder sb = new QueryBuilder();
		sb.append("select page.key, page.title, page.status, ")
		  .append("  binder.key, binder.title,")
		  .append("  v.key, ref.subIdent, v.displayname,")
		  .append("  media.key, mediaVersion.key, mediaVersion.versionName")
		  .append(" from cepage as page")
		  .append(" inner join page.body as pageBody")
		  .append(" inner join treat(pageBody.parts as cemediapart) mediaPart")
		  .append(" inner join mediaPart.media as media")
		  .append(" left join mediaPart.mediaVersion as mediaVersion")
		  .append(" left join page.section as section")
		  .append(" left join section.binder as binder")
		  .append(" left join cepagereference ref on (ref.page.key=page.key)")
		  .append(" left join ref.repositoryEntry as v")
		  .append(" where media.key=:mediaKey");
		
		List<Object[]> objects = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.setParameter("mediaKey", media.getKey())
				.getResultList();
		List<MediaUsage> usage = new ArrayList<>(objects.size());
		for(Object[] object:objects) {
			Long pageKey = (Long)object[0];
			String pageTitle = (String)object[1];
			String pageStatus = (String)object[2];
			
			Long binderKey = (Long)object[3];
			String binderTitle = (String)object[4];
			
			Long repoKey = (Long)object[5];
			String subIdent = (String)object[6];
			String repoDisplayname = (String)object[7];

			Long mediaKey = (Long)object[8];
			Long mediaVersionKey = (Long)object[9];
			String mediaVersionName = (String)object[10];
			
			usage.add(new MediaUsage(pageKey, pageTitle, pageStatus, binderKey, binderTitle,
					repoKey, subIdent, repoDisplayname,
					mediaKey, mediaVersionKey, mediaVersionName));
		}
		return usage;
	}
	
	public int deleteMedia(Media media) {
		Media reloadedMedia = loadByKey(media.getKey());
		if(reloadedMedia == null) return 0;
		
		int count = 0;
		List<MediaVersion> versions = reloadedMedia.getVersions();
		if(versions != null) {
			for(MediaVersion version:versions) {
				deleteMedia(version);
				count++;
			}
		}
		dbInstance.getCurrentEntityManager().remove(reloadedMedia);
		count++;
		return count;
	}
	
	private void deleteMedia(MediaVersion mediaVersion) {
		if(StringHelper.containsNonWhitespace(mediaVersion.getRootFilename())) {
			VFSContainer container = fileStorage.getMediaContainer(mediaVersion);
			VFSItem item = container.resolve(mediaVersion.getRootFilename());
			if(item instanceof VFSLeaf leaf) {
				leaf.delete();
			}
		}
		dbInstance.getCurrentEntityManager().remove(mediaVersion);
	}
}
