create_docker_tar:
	mkdir -p "out/lib/"
	ls -1 lib | grep -v "\(javadoc\|sources\)" | xargs -I {} cp lib/{} out/lib/
	tar -czvf out/Dockerfile.tar.gz Dockerfile -C out/ lib/

add_backslashes = '$$!s/$$/\\/'
add_folder = 's/^/\/lib\//'
add_colon = '1!s/^/:/'
list_jars:
	ls -1 lib | grep -v "\(javadoc\|sources\)" |\
	 sed $(add_backslashes) | sed $(add_folder) |\
	 sed $(add_colon)